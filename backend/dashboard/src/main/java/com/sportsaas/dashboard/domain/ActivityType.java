package com.sportsaas.dashboard.domain;

/**
 * Types d'activites trackees dans le systeme.
 */
public enum ActivityType {
    /** Connexion utilisateur */
    USER_LOGIN,
    /** Deconnexion utilisateur */
    USER_LOGOUT,
    /** Creation utilisateur */
    USER_CREATED,
    /** Mise a jour utilisateur */
    USER_UPDATED,
    /** Invitation utilisateur */
    USER_INVITED,

    /** Produit cree */
    PRODUCT_CREATED,
    /** Produit mis a jour */
    PRODUCT_UPDATED,
    /** Produit supprime */
    PRODUCT_DELETED,

    /** Commande creee */
    ORDER_CREATED,
    /** Commande mise a jour */
    ORDER_UPDATED,
    /** Commande payee */
    ORDER_PAID,
    /** Commande livree */
    ORDER_DELIVERED,
    /** Commande annulee */
    ORDER_CANCELLED,

    /** Location creee */
    RENTAL_CREATED,
    /** Location retournee */
    RENTAL_RETURNED,

    /** Facture creee */
    INVOICE_CREATED,
    /** Facture envoyee */
    INVOICE_SENT,
    /** Facture payee */
    INVOICE_PAID,

    /** Mouvement de stock */
    STOCK_MOVEMENT,
    /** Alerte stock bas */
    STOCK_LOW_ALERT,

    /** Paiement recu */
    PAYMENT_RECEIVED,

    /** Autre activite */
    OTHER
}
