package com.sportsaas.order.domain;

/**
 * Statuts d'une location.
 */
public enum RentalStatus {
    /** Reservation en attente */
    PENDING,
    /** Location confirmee */
    CONFIRMED,
    /** Equipement en cours d'utilisation */
    ACTIVE,
    /** Retour en attente */
    PENDING_RETURN,
    /** Retourne */
    RETURNED,
    /** Retour en retard */
    OVERDUE,
    /** Annule */
    CANCELLED
}
