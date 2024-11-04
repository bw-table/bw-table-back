package com.zero.bwtableback.reserve;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.zero.bwtableback.payment.PaymentCompleteRequest;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reserve")
@RequiredArgsConstructor
public class ReserveController {
    private final ReserveService reserveService; // 예약 처리 서비스
    private final HttpSession httpSession; // 세션 관리

    /**
     * 예약 정보를 임시 저장
     *
     * @param payload "date": "2024-11-05", "time": "19:00", "people": 2
     * @return
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
        } // 문자열을 정수로 변환

        System.out.println("임식 에약 생성 전");

        // 임시 예약 생성
        Reserve temporaryReservation = reserveService.createReservation(date, time, people);

        // 세션에 임시 예약 정보를 저장
        httpSession.setAttribute("temporaryReservation", temporaryReservation);

        System.out.println("임식 에약 저장 완료");

        System.out.println(date);
        System.out.println(time);
        System.out.println(people);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "예약이 성공적으로 생성되었습니다.");

        // 예약 정보를 응답에 추가
        String reservationId = UUID.randomUUID().toString();
        response.put("reservationId", reservationId);  // 임시 예약 번호 UUID
        response.put("date", temporaryReservation.getDate());
        response.put("time", temporaryReservation.getTime());
        response.put("people", temporaryReservation.getPeople());

        return ResponseEntity.ok(response);
    }

    /**
     * 결제 완료 처리
     */
    @PostMapping("/complete-payment")
    public ResponseEntity<Object> completePayment(@RequestBody PaymentCompleteRequest request) throws IamportResponseException, IOException {

        System.out.println("결제 완료!!");
        // 아임포트 API를 통해 결제 검증

        PaymentVerificationResponse verificationResponse = reserveService.verifyPayment(request.getImp_uid());

        if (!verificationResponse.isPaid()) {
            return ResponseEntity.badRequest().body("결제가 완료되지 않았습니다.");
        }

        // 예약 확정 로직 (임시 예약 정보 가져오기)
        Reserve temporaryReservation = (Reserve) httpSession.getAttribute("temporaryReservation");

        if (temporaryReservation == null) {
            return ResponseEntity.badRequest().body("임시 예약 정보를 찾을 수 없습니다.");
        }

        // 최종 예약 확정 처리
        Reserve completedReservation = reserveService.confirmReserve(temporaryReservation, request);

        // 응답 DTO 변환
        ReserveResDto reservationResponseDto = new ReserveResDto(completedReservation);

        return ResponseEntity.ok(reservationResponseDto);
    }

    /**
     * 최종 예약 테이블 생성
     */
//    @PostMapping("/done")
//    public ResponseEntity<Object> completeReservation(@RequestBody ReserveDto request) {
//        // 세션에서 임시 예약 정보를 가져옴
//        Reserve temporaryReservation = (Reserve) httpSession.getAttribute("temporaryReservation");
//
//        if (temporaryReservation == null) {
//            return ResponseEntity.badRequest().body("임시 예약 정보를 찾을 수 없습니다.");
//        }

    // 최종 예약 확정 처리
//        Reserve completedReservation = reserveService.confirmReserve(temporaryReservation, request);

    // 응답 DTO 변환
//        ReserveResDto reservationResponseDto = new ReserveResDto(completedReservation);

//        return ResponseEntity.ok(reservationResponseDto);
//        return null;
//    }
}