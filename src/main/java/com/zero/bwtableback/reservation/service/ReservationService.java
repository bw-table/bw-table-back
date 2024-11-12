package com.zero.bwtableback.reservation.service;

import static com.zero.bwtableback.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.zero.bwtableback.common.exception.ErrorCode.RESERVATION_FULL;
import static com.zero.bwtableback.common.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static com.zero.bwtableback.common.exception.ErrorCode.RESTAURANT_NOT_FOUND;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.ReservationRequestDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.repository.ReservationSpecifications;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> findReservationsWithFilters(Long restaurantId,
                                                                    Long memberId,
                                                                    ReservationStatus reservationStatus,
                                                                    LocalDate reservationDate,
                                                                    LocalTime reservationTime,
                                                                    Pageable pageable) {

        Specification<Reservation> spec = Specification.where(ReservationSpecifications.hasRestaurantId(restaurantId))
                .and(ReservationSpecifications.hasMemberId(memberId))
                .and(ReservationSpecifications.hasReservationStatus(reservationStatus))
                .and(ReservationSpecifications.hasReservationDate(reservationDate))
                .and(ReservationSpecifications.hasReservationTime(reservationTime));

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);

        return reservationPage.map(ReservationResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto reservationRequestDto,
                                                    Long restaurantId,
                                                    Long memberId) {
        validateReservationAvailability(restaurantId, reservationRequestDto);
        Restaurant restaurant = findRestaurantById(restaurantId);
        Member member = findMemberById(memberId);

        Reservation reservation = ReservationRequestDto.toEntity(reservationRequestDto, restaurant, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(savedReservation);
    }

    @Transactional
    public ReservationResponseDto confirmReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.confirmReservation();
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto cancelReservationByCustomer(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.cancelByCustomer();
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto cancelReservationByOwner(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.cancelByOwner();
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto markReservationAsNoShow(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.markAsNoShow();
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto markReservationAsVisited(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.markAsVisited();
        return ReservationResponseDto.fromEntity(reservation);
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
    }

    private Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    private void validateReservationAvailability(Long restaurantId, ReservationRequestDto reservationRequestDto) {
//        reservationRepository.findByRestaurantIdAndReservationDateAndReservationTime(
//                        restaurantId,
//                        reservationRequestDto.reservationDate(),
//                        reservationRequestDto.reservationTime())
//                .ifPresent(r -> {
//                    throw new CustomException(RESERVATION_FULL);
//                });
    }
}
