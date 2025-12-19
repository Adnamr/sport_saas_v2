package com.sportsaas.billing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Invoices.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status, Pageable pageable);

    List<Invoice> findByCustomerIdAndStatus(UUID customerId, InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.order.id = :orderId")
    Optional<Invoice> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT i FROM Invoice i WHERE i.rental.id = :rentalId")
    Optional<Invoice> findByRentalId(@Param("rentalId") UUID rentalId);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status NOT IN ('PAID', 'CANCELLED', 'REFUNDED')")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.status IN :statuses ORDER BY i.createdAt DESC")
    Page<Invoice> findByStatusIn(@Param("statuses") List<InvoiceStatus> statuses, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.issueDate BETWEEN :start AND :end ORDER BY i.issueDate DESC")
    List<Invoice> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.customerId = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.customerId = :customerId AND i.status = 'PAID'")
    BigDecimal sumPaidTotalByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.customerId = :customerId AND i.status NOT IN ('PAID', 'CANCELLED', 'REFUNDED')")
    BigDecimal sumBalanceDueByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Somme totale des factures payees (pour dashboard).
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal sumTotalPaid();

    /**
     * Somme des factures payees dans une periode.
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.paidAt BETWEEN :start AND :end")
    BigDecimal sumTotalPaidBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    /**
     * Somme des factures payees pour une date (par issueDate).
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issueDate = :date")
    BigDecimal sumTotalPaidByDate(@Param("date") LocalDate date);

    /**
     * Somme des factures payees pour un mois (par issueDate).
     */
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issueDate BETWEEN :start AND :end")
    BigDecimal sumTotalPaidByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
