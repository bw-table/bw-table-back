package com.zero.bwtableback.reserve;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.zero.bwtableback.payment.PaymentCompleteRequest;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reserve")
public class ReserveController {
    private final ReserveService reserveService; // 예약 처리 서비스
    private final HttpSession httpSession; // 세션 관리

    /**
     * 예약 정보 생성 및 세션에 임시 저장
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody Map<String, Object> payload) {
        String date = (String) payload.get("date");
        String time = (String) payload.get("time");

        // Object에서 문자열로 변환 후, 정수로 변환
        Integer people = null;

        try {
            people = Integer.parseInt(payload.get("people").toString());  // String으로 변환 후 Integer로 변환
        } catch (NumberFormatException e) {
            return null;
        }

        String specialRequest = (String) payload.get("special_request");

        System.out.println("임식 에약 생성 전");

        // 임시 예약 생성
        Reserve temporaryReservation = reserveService.createReservation(date, time, people, specialRequest);

        // 결제 전 세션에 임시 예약 정보를 저장
        httpSession.setAttribute("temporaryReservation", temporaryReservation);
        httpSession.setMaxInactiveInterval(600); // 세션 타임아웃을 10분(600초)으로 설정

        System.out.println("임시 에약 저장 완료");

        ReserveCreateResDto reserveCreateResDto = reserveService.createReserveResponseRes(
                "john.doe@example.com"
                , 1L
        );

        Map<String, Object> response = new HashMap<>();

        response.put("status", "success");
        response.put("message", "예약이 성공적으로 생성되었습니다.");

        response.put("reservation", temporaryReservation);
        response.put("data", reserveCreateResDto);

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 완료 처리
     *
     * KG이니시스 결제 완료와 함께 창이 닫히면서 실행
     */
    @PostMapping("/complete-payment")
    public ResponseEntity<Object> completePayment(@RequestBody PaymentCompleteRequest request) throws IamportResponseException, IOException {

        System.out.println("complete-payment!!");

        // 아임포트 API를 통해 결제 검증
        PaymentVerificationResponse verificationResponse = reserveService.verifyPayment(request.getImp_uid());

        if (!verificationResponse.isPaid()) {
            return ResponseEntity.badRequest().body("결제가 완료되지 않았습니다.");
        }

        System.out.println(verificationResponse);
        //TODO Payment 결제 정보 저장

        // 예약 확정 로직 (임시 예약 정보 가져오기)
        Reserve temporaryReservation = (Reserve) httpSession.getAttribute("temporaryReservation");

        temporaryReservation.setStatus(ReservationStatus.CONFIRMED);

        System.out.println(temporaryReservation);

        if (temporaryReservation == null) {
            return ResponseEntity.badRequest().body("임시 예약 정보를 찾을 수 없습니다.");
        }

        // 최종 예약 확정 처리
        // 응답 DTO 변환
        ReserveConfirmedResDto reserveConfirmedResDto =  reserveService.confirmReserve(temporaryReservation, request);

        // 예약이 저장되고 세션(max 10분)은 삭제 반환 값은 전부 다

        return ResponseEntity.ok(reserveConfirmedResDto);
    }
}

