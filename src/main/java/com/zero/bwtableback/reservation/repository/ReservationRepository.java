package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByReservationDateAndReservationStatus(
            LocalDate reservationDate,
            ReservationStatus reservationStatus
    );

    Page<Reservation> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 현재 예약 건수 확인
     * COALESCE는 합계가 NULL일 경우 0 반환
     */
    @Query("SELECT COALESCE(SUM(r.numberOfPeople), 0) FROM Reservation r " +
            "WHERE r.restaurant.id = :restaurantId " +
            "AND r.reservationDate = :reservationDate " +
            "AND r.reservationTime = :reservationTime " +
            "AND r.reservationStatus = 'CONFIRMED'")
            int countReservedPeopleByRestaurantAndDateTime(
            @Param("restaurantId") Long restaurantId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("reservationTime") LocalTime reservationTime
    );
    // FIXME 사용하지 않는다면 삭제
//    Optional<Reservation> findByMemberAndRestaurantAndReservationStatus(
//            Member member, Restaurant restaurant, ReservationStatus reservationStatus);
//    Optional<Reservation> findByMemberAndRestaurantAndReservationDateBetween(
//            Member member, Restaurant restaurant, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByRestaurantId(Long restaurantId);

    List<Reservation> findByRestaurantIdAndReservationDate(Long restaurantId, LocalDate reservationDate);

    Reservation findTopByMemberOrderByReservationDateDesc(Member member);

    // 날짜별 예약 건수 조회
    @Query("""
                SELECT r.reservationDate, COUNT(r)
                FROM Reservation r
                WHERE r.restaurant.id = :restaurantId
                  AND r.reservationDate BETWEEN :startDate AND :endDate
                  AND r.reservationStatus = 'VISITED'
                GROUP BY r.reservationDate
            """)
    List<Object[]> aggregateDailyStatistics(Long restaurantId, LocalDate startDate, LocalDate endDate);

    // 시간대별 예약 건수 조회
    @Query("""
                SELECT r.reservationTime, COUNT(r)
                FROM Reservation r
                WHERE r.restaurant.id = :restaurantId
                  AND r.reservationDate BETWEEN :startDate AND :endDate
                  AND r.reservationStatus = 'VISITED'
                GROUP BY r.reservationTime
            """)
    List<Object[]> aggregateTimeSlotStatistics(Long restaurantId, LocalDate startDate, LocalDate endDate);

    // 회원탈퇴 시 회원의 확정된 예약 조회
    List<Reservation> findAllByMemberIdAndReservationStatus(Long MemberId, ReservationStatus status);

}
