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
 * Repository pour les StockMovements.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    List<StockMovement> findByProductIdAndTypeOrderByCreatedAtDesc(UUID productId, MovementType type);

    Page<StockMovement> findByTypeOrderByCreatedAtDesc(MovementType type, Pageable pageable);

    @Query("SELECT m FROM StockMovement m WHERE m.createdAt BETWEEN :start AND :end ORDER BY m.createdAt DESC")
    List<StockMovement> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT m FROM StockMovement m WHERE m.reference = :reference")
    List<StockMovement> findByReference(@Param("reference") String reference);

    @Query("SELECT m FROM StockMovement m WHERE m.reservationId = :reservationId ORDER BY m.createdAt DESC")
    List<StockMovement> findByReservationId(@Param("reservationId") UUID reservationId);
}
