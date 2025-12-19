package com.sportsaas.dashboard.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour le graphique des revenus.
 */
public record RevenueChartResponse(
    /** Periode du graphique (ex: "last_12_months", "last_30_days") */
    String period,
    /** Labels de l'axe X (dates/mois) */
    List<String> labels,
    /** Valeurs des revenus */
    List<BigDecimal> revenues,
    /** Total sur la periode */
    BigDecimal total,
    /** Moyenne sur la periode */
    BigDecimal average
) {}
