package com.sportsaas.billing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Payments.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    boolean existsByPaymentReference(String paymentReference);

    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId ORDER BY p.createdAt DESC")
    List<Payment> findByInvoiceId(@Param("invoiceId") UUID invoiceId);

    Page<Payment> findByMethodOrderByCreatedAtDesc(PaymentMethod method, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customerId = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customerId = :customerId AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedAmountByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.id = :invoiceId AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
