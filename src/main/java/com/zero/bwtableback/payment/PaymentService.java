package com.zero.bwtableback.payment;

import com.google.gson.JsonObject;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.payment.entity.PaymentEntity;
import com.zero.bwtableback.payment.entity.PaymentStatus;
import com.zero.bwtableback.reservation.dto.PaymentReqDto;
import com.zero.bwtableback.reservation.dto.ReservationCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationCreateReqDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${IMP_API_KEY}")
    private String apiKey;

    @Value("${IMP_API_SECRET}")
    private String secretKey;

    @Value("${IMP_CODE}")
    private String impUid;

    private IamportClient iamportClient;

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * 아임포트에 대한 결제 검증
     */
    public Payment verifyPayment(PaymentReqDto paymentReqDto) {
        try {
            this.iamportClient = new IamportClient(apiKey, secretKey);
            // 아임포트 API를 통해 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(paymentReqDto.getImpUid());

            // 응답에서 결제 정보가 있는지 확인
            if (iamportResponse.getResponse() != null) {
                Payment payment = iamportResponse.getResponse();
                System.out.println(PaymentStatus.PAID + payment.getStatus());
                // 결제 상태 체크
                if (!"paid".equals(payment.getStatus())) {
                    throw new CustomException(ErrorCode.PAYMENT_NOT_COMPLETED);
                }
                return payment;
            } else {
                log.error("결제 정보가 없습니다.");
                throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("결제 검증 중 오류가 발생했습니다: " + e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_VERIFY_ERROR);
        }
    }

    public void verifiedPaymentSave(Payment payment, ReservationCompleteResDto response){
        PaymentEntity paymentEntity = convertToPaymentEntity(payment, response);
        paymentRepository.save(paymentEntity);
    }

    private PaymentEntity convertToPaymentEntity(Payment payment, ReservationCompleteResDto response) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setImpUid(payment.getImpUid());
        paymentEntity.setMerchantUid(payment.getMerchantUid());
        paymentEntity.setBuyerName(payment.getBuyerName());
        paymentEntity.setBuyerEmail(payment.getBuyerEmail());
        paymentEntity.setBuyerTel(payment.getBuyerTel());
        paymentEntity.setCardName(payment.getCardName());
        paymentEntity.setCardNumber(payment.getCardNumber());
        paymentEntity.setPaidAmount(payment.getAmount());
        paymentEntity.setCurrency(payment.getCurrency());
        paymentEntity.setStatus(PaymentStatus.valueOf(payment.getStatus().toUpperCase())); // 결제 상태
        paymentEntity.setPaidAt(payment.getPaidAt());
        paymentEntity.setReceiptUrl(payment.getReceiptUrl());
        paymentEntity.setReservation(findReservationById(response.getReservation().reservationId()));
        paymentEntity.setRestaurant(findRestaurantById(response.getReservation().restaurantId()));

        return paymentEntity;
    }

    public Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    /**
     * 결제된 예약금 환불
     *
     * PAID 인지 확인하기
     * 환불 규정
     * 1일전 환불 불가
     * 2일전 50%
     * 3일전 100%
     */
    public void refundReservationDeposit(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        int deposit = reservation.getRestaurant().getDeposit();

        HttpsURLConnection conn = null;
        try {
            URL url = new URL("https://api.iamport.kr/payments/cancel");
            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true); // OutputStream으로 데이터 전달

            JsonObject json = new JsonObject();
            json.addProperty("reason", "방문 완료");
            json.addProperty("imp_uid", impUid);
            json.addProperty("amount", deposit);
            json.addProperty("checksum", deposit);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                bw.write(json.toString());
                bw.flush();
            }

            // FIXME 아임포트 응답 오류 해결
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
//                System.out.println(br);
//                String responseLine;
//                StringBuilder response = new StringBuilder();
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine);
//                }
//                System.out.println("Response: " + response.toString());
//            }
            PaymentEntity payment = paymentRepository.findByReservationId(reservationId)
                    .orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

            payment.setStatus(PaymentStatus.REFUNDED);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
