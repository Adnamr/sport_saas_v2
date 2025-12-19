package com.sportsaas.dashboard.api.dto;

import java.math.BigDecimal;

/**
 * DTO pour les statistiques globales du dashboard.
 */
public record DashboardStatsResponse(
    /** Chiffre d'affaires total (factures payees) */
    BigDecimal totalRevenue,
    /** Chiffre d'affaires du mois en cours */
    BigDecimal monthlyRevenue,
    /** Evolution du CA par rapport au mois precedent (en %) */
    BigDecimal revenueGrowth,

    /** Nombre total de commandes */
    long totalOrders,
    /** Commandes du mois en cours */
    long monthlyOrders,
    /** Commandes en attente */
    long pendingOrders,

    /** Nombre total de produits */
    long totalProducts,
    /** Produits en stock bas */
    long lowStockProducts,
    /** Produits en rupture */
    long outOfStockProducts,

    /** Nombre total de clients */
    long totalCustomers,
    /** Nouveaux clients ce mois */
    long newCustomersThisMonth,

    /** Nombre de locations actives */
    long activeRentals
) {}
