package com.sportsaas.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Rentals.
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {

    Optional<Rental> findByRentalNumber(String rentalNumber);

    boolean existsByRentalNumber(String rentalNumber);

    Page<Rental> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status, Pageable pageable);

    Page<Rental> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    List<Rental> findByCustomerIdAndStatus(UUID customerId, RentalStatus status);

    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate < :now")
    List<Rental> findOverdueRentals(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Rental r WHERE r.status IN ('PENDING', 'CONFIRMED') AND r.startDate <= :date")
    List<Rental> findUpcomingRentals(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Rental r WHERE r.product.id = :productId AND r.status IN ('ACTIVE', 'CONFIRMED', 'PENDING')")
    List<Rental> findActiveRentalsByProductId(@Param("productId") UUID productId);

    @Query("SELECT SUM(r.quantity) FROM Rental r WHERE r.product.id = :productId AND r.status IN ('ACTIVE', 'CONFIRMED')")
    Integer sumActiveQuantityByProductId(@Param("productId") UUID productId);

    @Query("SELECT r FROM Rental r WHERE r.startDate BETWEEN :start AND :end ORDER BY r.startDate ASC")
    List<Rental> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
