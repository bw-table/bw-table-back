package com.zero.bwtableback.payment;

import com.google.gson.JsonObject;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.payment.entity.PaymentEntity;
import com.zero.bwtableback.payment.entity.PaymentStatus;
import com.zero.bwtableback.reservation.dto.PaymentResDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
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

    private IamportClient iamportClient;

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;


    public boolean verifyPaymentAndSave(PaymentResDto paymentResDto) {
        // TODO Payment 레코드에 회원 아이디,가게아이디,예약아이디
        try {
            this.iamportClient = new IamportClient(apiKey, secretKey);
            // 아임포트 API를 통해 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(paymentResDto.getImpUid());

            // 응답에서 결제 정보가 있는지 확인
            if (iamportResponse.getResponse() != null) {
                Payment payment = iamportResponse.getResponse();
                PaymentEntity paymentEntity = convertToPaymentEntity(payment);
                paymentRepository.save(paymentEntity);
                // 결제 상태 체크
                if (!PaymentStatus.PAID.equals(paymentEntity.getStatus())) {
                    return false;
                }
                return true;
            } else {
                log.error("결제 정보가 없습니다.");
                return false;
            }
        } catch (Exception e) {
            log.error("결제 검증 중 오류가 발생했습니다: " + e.getMessage());
            return false;
        }
    }

    private PaymentEntity convertToPaymentEntity(Payment payment) {
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

        return paymentEntity;
    }

    /**
     * 결제된 예약금 환불
     */
    public void refundReservationDeposit(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        String impUid = reservation.getRestaurant().getImpCode();
        int deposit = reservation.getRestaurant().getDeposit();

        HttpsURLConnection conn = null;
        try {
            URL url = new URL("https://api.iamport.kr/payments/cancel");
            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JsonObject json = new JsonObject();
            json.addProperty("reason", "방문 완료");
            json.addProperty("imp_uid", impUid);
            json.addProperty("amount", deposit);
            json.addProperty("checksum", deposit);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                bw.write(json.toString());
                bw.flush();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                StringBuilder response = new StringBuilder();
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                System.out.println("Response: " + response.toString());
            }

        } catch (IOException e) {
            throw new CustomException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
