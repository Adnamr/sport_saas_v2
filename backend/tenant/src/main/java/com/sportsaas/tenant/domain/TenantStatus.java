package com.sportsaas.tenant.domain;

/**
 * Statuts possibles d'un tenant.
 */
public enum TenantStatus {
    /**
     * Tenant actif et opérationnel.
     */
    ACTIVE,

    /**
     * Tenant suspendu temporairement.
     */
    SUSPENDED,

    /**
     * Tenant en cours de configuration.
     */
    PENDING,

    /**
     * Tenant archivé/supprimé.
     */
    ARCHIVED
}
