package com.sportsaas.notification.domain;

/**
 * Statuts d'envoi d'email.
 */
public enum EmailStatus {
    /** En attente d'envoi */
    PENDING,

    /** En cours d'envoi */
    SENDING,

    /** Envoye avec succes */
    SENT,

    /** Echec d'envoi */
    FAILED,

    /** Rejete (bounce) */
    BOUNCED
}
