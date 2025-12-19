package com.sportsaas.admin.domain;

/**
 * Statuts d'un tenant sur la plateforme.
 */
public enum TenantStatus {
    /** Tenant actif */
    ACTIVE,

    /** Tenant suspendu (paiement en retard, etc.) */
    SUSPENDED,

    /** Tenant en periode d'essai */
    TRIAL,

    /** Tenant desactive */
    INACTIVE,

    /** Tenant en attente de validation */
    PENDING_ACTIVATION
}
