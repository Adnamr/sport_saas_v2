package com.sportsaas.admin.domain;

/**
 * Plans d'abonnement disponibles.
 */
public enum SubscriptionPlan {
    /** Plan gratuit avec limitations */
    FREE,

    /** Plan de base */
    STARTER,

    /** Plan professionnel */
    PROFESSIONAL,

    /** Plan entreprise */
    ENTERPRISE,

    /** Plan personnalise */
    CUSTOM
}
