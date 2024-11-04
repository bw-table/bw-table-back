package com.zero.bwtableback.reserve;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.payment.PaymentCompleteRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReserveService {
    private final ReserveRepository reserveRepository;

    @Value("${IMP_API_KEY}")
    private String apiKey;

    @Value("${IMP_API_SECRET}")
    private String secretKey;

    private IamportClient iamportClient;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    /**
     * 임시 예약 생성 메서드
     */
    public Reserve createReservation(String date, String time, Integer people) {
        Reserve reservation = Reserve.builder()
                .date(date)
                .time(time)
                .people(people)
                .build();
        return reservation; // 임시 예약 객체 반환 (세션에 저장할 것임)
    }

    /**
     * 결제 검증 메서드
     */
    public PaymentVerificationResponse verifyPayment(String impUid) {
        try {
            // 아임포트 API를 통해 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(impUid);

            // 응답에서 결제 정보가 있는지 확인
            if (iamportResponse.getResponse() != null) {
                Payment payment = iamportResponse.getResponse();

                // PaymentVerificationResponse 객체 생성 및 데이터 설정
                PaymentVerificationResponse response = new PaymentVerificationResponse();
                response.setImp_uid(payment.getImpUid());
                response.setMerchant_uid(payment.getMerchantUid());
                response.setStatus(payment.getStatus());
                response.setAmount(payment.getAmount());
                response.setCurrency(payment.getCurrency());
                response.setBuyer_name(payment.getBuyerName());
                response.setBuyer_email(payment.getBuyerEmail());
                response.setBuyer_tel(payment.getBuyerTel());

                return response; // 결제 정보 반환
            } else {
                throw new RuntimeException("결제 정보가 없습니다.");
            }
        } catch (Exception e) {
            // 예외 처리: 로그 기록 및 적절한 예외 반환
            throw new RuntimeException("결제 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 최종 예약 확정 메서드
     */
    @Transactional
    public Reserve confirmReserve(Reserve temporaryReserve, PaymentCompleteRequest request) {
        // 필요한 정보로 최종 예약 객체 생성 후 저장
        Reserve completedReserve = new Reserve();
        completedReserve.setDate(temporaryReserve.getDate());
        completedReserve.setTime(temporaryReserve.getTime());
        completedReserve.setPeople(temporaryReserve.getPeople());

        return reserveRepository.save(completedReserve); // DB에 저장 후 반환
    }
}