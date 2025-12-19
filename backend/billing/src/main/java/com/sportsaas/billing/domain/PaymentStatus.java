package com.sportsaas.billing.domain;

/**
 * Statuts possibles d'un paiement.
 */
public enum PaymentStatus {
    /** En attente de traitement */
    PENDING,
    /** En cours de traitement */
    PROCESSING,
    /** Paiement reussi */
    COMPLETED,
    /** Paiement echoue */
    FAILED,
    /** Paiement annule */
    CANCELLED,
    /** Rembourse */
    REFUNDED
}
