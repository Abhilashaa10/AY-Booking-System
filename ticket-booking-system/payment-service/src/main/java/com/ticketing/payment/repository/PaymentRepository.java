package com.ticketing.payment.repository;

import com.ticketing.payment.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByBookingId(UUID bookingId);

    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

    boolean existsByBookingId(UUID bookingId);
}