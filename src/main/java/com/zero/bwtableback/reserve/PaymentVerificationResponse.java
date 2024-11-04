package com.zero.bwtableback.reserve;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentVerificationResponse {
    private String imp_uid; // 아임포트에서 받은 결제 고유 번호
    private String merchant_uid; // 주문 ID
    private String status; // 결제 상태 (예: paid, canceled 등)
    private BigDecimal amount; // 결제 금액
    private String currency; // 통화 (예: KRW)
    private String buyer_name; // 구매자 이름
    private String buyer_email; // 구매자 이메일
    private String buyer_tel; // 구매자 전화번호

    public boolean isPaid() {
        return "paid".equals(status); // status가 "paid"인 경우 true 반환
    }
}