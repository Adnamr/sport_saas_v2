package com.sportsaas.notification.domain;

/**
 * Types d'emails supportes.
 */
public enum EmailType {
    /** Email de bienvenue */
    WELCOME,

    /** Reinitialisation du mot de passe */
    PASSWORD_RESET,

    /** Confirmation de commande */
    ORDER_CONFIRMATION,

    /** Confirmation de location */
    RENTAL_CONFIRMATION,

    /** Rappel de retour de location */
    RENTAL_RETURN_REMINDER,

    /** Facture */
    INVOICE,

    /** Confirmation de paiement */
    PAYMENT_CONFIRMATION,

    /** Rappel de paiement */
    PAYMENT_REMINDER,

    /** Invitation utilisateur */
    INVITATION,

    /** Email generique */
    GENERIC
}
