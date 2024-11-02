package com.zero.bwtableback.reservation.service;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.dto.ReservationRequestDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.repository.ReservationSpecifications;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    // 조건부 필터로 예약 조회
    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> findReservationsWithFilters(Long restaurantId, Long memberId,
                                                                    ReservationStatus reservationStatus, LocalDate reservationDate,
                                                                    LocalTime reservationTime, Pageable pageable) {

        // 필터 조건 생성
        Specification<Reservation> spec = Specification.where(ReservationSpecifications.hasRestaurantId(restaurantId))
                .and(ReservationSpecifications.hasMemberId(memberId))
                .and(ReservationSpecifications.hasReservationStatus(reservationStatus))
                .and(ReservationSpecifications.hasReservationDate(reservationDate))
                .and(ReservationSpecifications.hasReservationTime(reservationTime));

        // 필터 조건과 페이지네이션 적용해서 조회
        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);

        // 엔티티 리스트를 DTO 리스트로 변환
        List<ReservationResponseDto> responseDtos = reservationPage.getContent().stream()
                .map(ReservationResponseDto::fromEntity)
                .collect(Collectors.toList());

        // DTO 리스트를 페이지 형태로 반환
        return new PageImpl<>(responseDtos, pageable, reservationPage.getTotalElements());
    }

    // 특정 예약 상세 조회
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));

        return ReservationResponseDto.fromEntity(reservation);
    }

    // 새로운 예약 생성
    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto reservationRequestDto,
                                                    Restaurant restaurant,
                                                    Member member) {
        Reservation reservation = ReservationRequestDto.toEntity(reservationRequestDto, restaurant, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(savedReservation);
    }

    // 예약 확정
    @Transactional
    public ReservationResponseDto confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.confirmReservation();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // 고객 취소
    @Transactional
    public ReservationResponseDto cancelReservationByCustomer(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.cancelByCustomer();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // 가게 취소
    @Transactional
    public ReservationResponseDto cancelReservationByOwner(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.cancelByOwner();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // 노쇼 처리
    @Transactional
    public ReservationResponseDto markReservationAsNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.markAsNoShow();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // 방문 완료 처리
    @Transactional
    public ReservationResponseDto markReservationAsVisited(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.markAsVisited();
        return ReservationResponseDto.fromEntity(reservation);
    }
    
}

