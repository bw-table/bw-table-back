package com.zero.bwtableback.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCompleteRequest {
    private String imp_uid; // 아임포트에서 받은 결제 고유 번호
    private String merchant_uid; // 주문 ID

}