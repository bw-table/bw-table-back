package com.zero.bwtableback.payment;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.zero.bwtableback.reserve.ReserveResDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {

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
     * 결제 준비
     */
//    @PostMapping("/prepare")
//    public ResponseEntity<?> preparePayment(@RequestBody PaymentPrepareRequest request) {
//        // 결제 준비 로직
//        return null;
//    }

//    /**
//     * 결제 확인
//     */
//    @GetMapping("/verify/{imp_uid}")
//    public IamportResponse<?> verifyPayment(@PathVariable String imp_uid) throws IamportResponseException, IOException {
//        return iamportClient.paymentByImpUid(imp_uid); // imp_uid를 검사하고, 데이터를 보내줌
//    }

    /**
     * 결제 완료 및 예약 확정
     */
//    @PostMapping("/complete")
//    public ResponseEntity<?> completePayment(@RequestBody PaymentCompleteRequest request) {
//        System.out.println(request);
//        return null;
////        return ResponseEntity.ok(new ReserveResDto(completedReservation));
//    }

    /**
     * 결제 취소
     */
//    @PostMapping("/cancel")
//    public ResponseEntity<?> cancelPayment(@RequestBody PaymentCancelRequest request) {
//        // 결제 취소 로직
//        return null;
//    }

    /**
     * 결제 상태 조회 로직
     */
//    @GetMapping("/status/{merchant_uid}")
//    public ResponseEntity<?> getPaymentStatus(@PathVariable String merchant_uid) {
//        return null;
//    }
}
