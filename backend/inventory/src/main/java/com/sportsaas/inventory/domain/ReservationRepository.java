package com.sportsaas.inventory.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour les Reservations.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Page<Reservation> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    List<Reservation> findByProductIdAndStatus(UUID productId, ReservationStatus status);

    Page<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status, Pageable pageable);

    List<Reservation> findByCustomerId(UUID customerId);

    List<Reservation> findByCustomerIdAndStatus(UUID customerId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.reference = :reference")
    List<Reservation> findByReference(@Param("reference") String reference);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(r.quantity) FROM Reservation r WHERE r.product.id = :productId AND r.status = 'ACTIVE'")
    Integer sumActiveReservationsByProductId(@Param("productId") UUID productId);
}
