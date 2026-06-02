package com.ticketing.cancellation.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CancellationRepository extends JpaRepository<CancellationEntity, UUID> {

    Optional<CancellationEntity> findByBookingId(UUID bookingId);

    boolean existsByBookingId(UUID bookingId);
}