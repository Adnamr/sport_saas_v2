package com.sportsaas.billing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des factures.
 */
public interface InvoiceService {

    /**
     * Cree une nouvelle facture.
     */
    Invoice create(Invoice invoice);

    /**
     * Cree une facture a partir d'une commande.
     */
    Invoice createFromOrder(UUID orderId);

    /**
     * Cree une facture a partir d'une location.
     */
    Invoice createFromRental(UUID rentalId);

    /**
     * Recherche une facture par ID.
     */
    Optional<Invoice> findById(UUID id);

    /**
     * Recherche une facture par numero.
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Liste les factures d'un client.
     */
    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Liste les factures par statut.
     */
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Ajoute un item a la facture.
     */
    Invoice addItem(UUID invoiceId, String description, int quantity, BigDecimal unitPrice);

    /**
     * Met a jour un item.
     */
    Invoice updateItem(UUID invoiceId, UUID itemId, int quantity, BigDecimal unitPrice);

    /**
     * Supprime un item.
     */
    Invoice removeItem(UUID invoiceId, UUID itemId);

    /**
     * Finalise la facture (prete a etre envoyee).
     */
    Invoice finalize(UUID id);

    /**
     * Marque la facture comme envoyee.
     */
    Invoice send(UUID id);

    /**
     * Applique une remise.
     */
    Invoice applyDiscount(UUID id, BigDecimal amount);

    /**
     * Definit le taux de TVA.
     */
    Invoice setTaxRate(UUID id, BigDecimal rate);

    /**
     * Annule la facture.
     */
    Invoice cancel(UUID id, String reason);

    /**
     * Recherche les factures en retard.
     */
    List<Invoice> findOverdue();

    /**
     * Traite les factures en retard.
     */
    int processOverdueInvoices();

    /**
     * Retourne les statistiques d'un client.
     */
    CustomerBillingStats getCustomerStats(UUID customerId);

    /**
     * Statistiques de facturation client.
     */
    record CustomerBillingStats(
            long invoiceCount,
            BigDecimal totalPaid,
            BigDecimal balanceDue
    ) {}
}
