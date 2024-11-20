package com.zero.bwtableback.payment;

import com.zero.bwtableback.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
        Optional<PaymentEntity> findByReservationId(Long ReservationId);
}
