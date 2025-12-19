package com.sportsaas.billing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des paiements.
 */
public interface PaymentService {

    /**
     * Cree un nouveau paiement.
     */
    Payment create(Payment payment);

    /**
     * Cree un paiement pour une facture.
     */
    Payment createForInvoice(UUID invoiceId, BigDecimal amount, PaymentMethod method);

    /**
     * Recherche un paiement par ID.
     */
    Optional<Payment> findById(UUID id);

    /**
     * Recherche un paiement par reference.
     */
    Optional<Payment> findByPaymentReference(String reference);

    /**
     * Liste les paiements d'un client.
     */
    Page<Payment> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Liste les paiements par statut.
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Liste les paiements d'une facture.
     */
    List<Payment> findByInvoiceId(UUID invoiceId);

    /**
     * Marque le paiement comme en cours de traitement.
     */
    Payment process(UUID id);

    /**
     * Marque le paiement comme complete.
     */
    Payment complete(UUID id, String externalReference);

    /**
     * Marque le paiement comme echoue.
     */
    Payment fail(UUID id, String reason);

    /**
     * Annule le paiement.
     */
    Payment cancel(UUID id);

    /**
     * Effectue un remboursement.
     */
    Payment refund(UUID id, BigDecimal amount, String reason);

    /**
     * Retourne les statistiques d'un client.
     */
    CustomerPaymentStats getCustomerStats(UUID customerId);

    /**
     * Statistiques de paiement client.
     */
    record CustomerPaymentStats(
            long paymentCount,
            BigDecimal totalPaid
    ) {}
}
