package com.sportsaas.inventory.domain;

/**
 * Statuts d'une reservation.
 */
public enum ReservationStatus {
    /** Reservation active */
    ACTIVE,
    /** Reservation confirmee (commande validee) */
    CONFIRMED,
    /** Reservation annulee */
    CANCELLED,
    /** Reservation expiree */
    EXPIRED,
    /** Reservation liberee (stock rendu) */
    RELEASED
}
