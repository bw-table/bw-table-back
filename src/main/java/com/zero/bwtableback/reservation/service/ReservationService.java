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

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> findReservationsWithFilters(Long restaurantId, Long memberId,
                                                                    ReservationStatus reservationStatus, LocalDate reservationDate,
                                                                    LocalTime reservationTime, Pageable pageable) {

        Specification<Reservation> spec = Specification.where(ReservationSpecifications.hasRestaurantId(restaurantId))
                .and(ReservationSpecifications.hasMemberId(memberId))
                .and(ReservationSpecifications.hasReservationStatus(reservationStatus))
                .and(ReservationSpecifications.hasReservationDate(reservationDate))
                .and(ReservationSpecifications.hasReservationTime(reservationTime));

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);

        List<ReservationResponseDto> responseDtos = reservationPage.getContent().stream()
                .map(ReservationResponseDto::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responseDtos, pageable, reservationPage.getTotalElements());
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));

        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto reservationRequestDto,
                                                    Restaurant restaurant,
                                                    Member member) {
        Reservation reservation = ReservationRequestDto.toEntity(reservationRequestDto, restaurant, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(savedReservation);
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional
    public ReservationResponseDto confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.confirmReservation();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional
    public ReservationResponseDto cancelReservationByCustomer(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.cancelByCustomer();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional
    public ReservationResponseDto cancelReservationByOwner(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.cancelByOwner();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional
    public ReservationResponseDto markReservationAsNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.markAsNoShow();
        return ReservationResponseDto.fromEntity(reservation);
    }

    // TODO: 커스텀 예외 추가 필요
    @Transactional
    public ReservationResponseDto markReservationAsVisited(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다: " + reservationId));
        reservation.markAsVisited();
        return ReservationResponseDto.fromEntity(reservation);
    }
    
}

