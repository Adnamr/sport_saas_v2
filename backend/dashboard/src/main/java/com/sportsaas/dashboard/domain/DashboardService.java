package com.sportsaas.dashboard.domain;

import com.sportsaas.dashboard.api.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service pour les donnees du dashboard.
 */
public interface DashboardService {

    /**
     * Recupere les statistiques globales.
     */
    DashboardStatsResponse getStats();

    /**
     * Recupere le graphique des revenus.
     * @param period Periode: "last_12_months", "last_30_days", "last_7_days"
     */
    RevenueChartResponse getRevenueChart(String period);

    /**
     * Recupere la repartition des commandes par statut.
     */
    OrdersByStatusResponse getOrdersByStatus();

    /**
     * Recupere les commandes recentes.
     */
    List<RecentOrderResponse> getRecentOrders(int limit);

    /**
     * Recupere les produits en stock bas.
     * @param limit Nombre maximum d'items a retourner (defaut 20)
     */
    List<LowStockItemResponse> getLowStockItems(int limit);

    /**
     * Recupere les activites recentes.
     */
    Page<ActivityResponse> getRecentActivities(Pageable pageable);

    /**
     * Enregistre une activite.
     */
    Activity logActivity(Activity activity);
}
