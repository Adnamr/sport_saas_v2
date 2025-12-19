package com.sportsaas.inventory.domain;

/**
 * Types de mouvements de stock.
 */
public enum MovementType {
    /** Entree de stock (achat, retour client) */
    IN,
    /** Sortie de stock (vente, perte) */
    OUT,
    /** Ajustement d'inventaire */
    ADJUSTMENT,
    /** Reservation pour une commande */
    RESERVATION,
    /** Liberation d'une reservation */
    RELEASE,
    /** Transfert entre emplacements */
    TRANSFER
}
