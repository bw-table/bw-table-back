package com.zero.bwtableback.reserve;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.payment.PaymentCompleteRequest;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReserveService {
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReserveRepository reserveRepository;

    private final HttpSession httpSession;

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
    public Reserve createReservation(String date, String time, Integer people, String specialRequest) {
        Reserve reservation = Reserve.builder()
                .date(date)
                .time(time)
                .people(people)
                .specialRequest(specialRequest)
                .status(ReservationStatus.CONFIRMED)
                .build();
        return reservation; // 임시 예약 객체 반환 (세션에 저장할 것임)
    }

    /**
     * 반환 값 생성
     */
    public ReserveCreateResDto createReserveResponseRes(String email, Long restaurantId) {
        // 회원 정보
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 가게 정보
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("레스토랑 없습니다."));

        // 객체에 담기
        ReserveCreateResDto response = new ReserveCreateResDto(member, restaurant);

        return response;
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
    public ReserveConfirmedResDto confirmReserve(Reserve temporaryReserve, PaymentCompleteRequest request) {
        System.out.println(request);
        // TODO payment에 저장

        // 가게 정보
        Restaurant restaurant = restaurantRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("레스토랑 없습니다."));

        // 필요한 정보로 최종 예약 객체 생성 후 저장
        ReserveConfirmedResDto response = new ReserveConfirmedResDto(
                restaurant,
                temporaryReserve
        );

        reserveRepository.save(temporaryReserve); // DB에 저장 후 반환

        // 세션에 저장된 값 삭제
        httpSession.removeAttribute("temporaryReservation");

        return response;
    }
}