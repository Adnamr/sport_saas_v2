package com.sportsaas.admin.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service d'administration de la plateforme.
 */
public interface AdminService {

    // ========== Gestion des tenants ==========

    /**
     * Cree un nouveau tenant.
     */
    TenantInfo createTenant(TenantInfo tenantInfo);

    /**
     * Recupere un tenant par ID.
     */
    Optional<TenantInfo> findTenantById(UUID id);

    /**
     * Liste tous les tenants.
     */
    Page<TenantInfo> findAllTenants(Pageable pageable);

    /**
     * Liste les tenants par statut.
     */
    Page<TenantInfo> findTenantsByStatus(TenantStatus status, Pageable pageable);

    /**
     * Liste les tenants par plan.
     */
    Page<TenantInfo> findTenantsByPlan(SubscriptionPlan plan, Pageable pageable);

    /**
     * Recherche des tenants.
     */
    Page<TenantInfo> searchTenants(String query, Pageable pageable);

    /**
     * Met a jour un tenant.
     */
    TenantInfo updateTenant(UUID id, TenantInfo updates);

    /**
     * Active un tenant.
     */
    TenantInfo activateTenant(UUID id);

    /**
     * Suspend un tenant.
     */
    TenantInfo suspendTenant(UUID id, String reason);

    /**
     * Desactive un tenant.
     */
    TenantInfo deactivateTenant(UUID id);

    /**
     * Change le plan d'abonnement.
     */
    TenantInfo changePlan(UUID id, SubscriptionPlan plan);

    /**
     * Met a jour les limites d'un tenant.
     */
    TenantInfo updateLimits(UUID id, Integer maxUsers, Integer maxProducts, Integer maxStorageMb);

    // ========== Statistiques plateforme ==========

    /**
     * Statistiques de la plateforme.
     */
    record PlatformStats(
        long totalTenants,
        long activeTenants,
        long suspendedTenants,
        long trialTenants,
        long totalUsers,
        long totalProducts,
        long totalOrders,
        long totalRentals
    ) {}

    /**
     * Recupere les statistiques de la plateforme.
     */
    PlatformStats getPlatformStats();

    /**
     * Statistiques par plan.
     */
    record PlanStats(SubscriptionPlan plan, long count) {}

    /**
     * Recupere les statistiques par plan.
     */
    List<PlanStats> getStatsByPlan();

    // ========== Maintenance ==========

    /**
     * Traite les abonnements expires.
     */
    int processExpiredSubscriptions();

    /**
     * Envoie des rappels pour les abonnements expirant bientot.
     */
    int sendExpirationReminders(int daysBeforeExpiry);
}
