package com.zero.bwtableback.payment;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.payment.entity.PaymentEntity;
import com.zero.bwtableback.payment.entity.PaymentStatus;
import com.zero.bwtableback.reservation.dto.PaymentResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


    public boolean verifyPaymentAndSave(PaymentResDto paymentResDto) {
        try {
            // FIXME 테스트 true 반환
            if (paymentResDto.getImpUid() != null) {
                return true;
            }
            // 아임포트 API를 통해 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(paymentResDto.getImpUid());

            // 응답에서 결제 정보가 있는지 확인
            if (iamportResponse.getResponse() != null) {
                Payment payment = iamportResponse.getResponse();

                PaymentEntity paymentEntity = convertToPaymentEntity(payment);

                paymentRepository.save(paymentEntity);

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
}
