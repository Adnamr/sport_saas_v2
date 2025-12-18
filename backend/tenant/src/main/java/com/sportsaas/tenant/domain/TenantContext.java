package com.sportsaas.tenant.domain;

import java.util.UUID;

/**
 * Contexte tenant stocké en ThreadLocal.
 * Permet d'accéder au tenant courant depuis n'importe où dans le code.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * Définit le tenant courant pour le thread.
     */
    public static void setCurrentTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Récupère l'ID du tenant courant.
     */
    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Vérifie si un tenant est défini.
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Nettoie le contexte tenant (important pour éviter les fuites mémoire).
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Récupère le tenant courant ou lance une exception.
     */
    public static UUID requireCurrentTenant() {
        UUID tenantId = getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Aucun tenant défini dans le contexte");
        }
        return tenantId;
    }
}
