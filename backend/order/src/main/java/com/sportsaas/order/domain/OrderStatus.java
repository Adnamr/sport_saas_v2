package com.sportsaas.order.domain;

/**
 * Statuts d'une commande.
 */
public enum OrderStatus {
    /** Commande en cours de creation */
    DRAFT,
    /** Commande en attente de paiement */
    PENDING_PAYMENT,
    /** Paiement confirme */
    PAID,
    /** En preparation */
    PROCESSING,
    /** Pret pour retrait/livraison */
    READY,
    /** Expedie */
    SHIPPED,
    /** Livre */
    DELIVERED,
    /** Annule */
    CANCELLED,
    /** Rembourse */
    REFUNDED
}
