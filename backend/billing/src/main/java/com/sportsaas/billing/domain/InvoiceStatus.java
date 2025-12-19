package com.sportsaas.billing.domain;

/**
 * Statuts possibles d'une facture.
 */
public enum InvoiceStatus {
    /** Brouillon - en cours de creation */
    DRAFT,
    /** Finalisee - prete a etre envoyee */
    FINALIZED,
    /** Envoyee au client */
    SENT,
    /** Partiellement payee */
    PARTIALLY_PAID,
    /** Entierement payee */
    PAID,
    /** En retard de paiement */
    OVERDUE,
    /** Annulee */
    CANCELLED,
    /** Remboursee */
    REFUNDED
}
