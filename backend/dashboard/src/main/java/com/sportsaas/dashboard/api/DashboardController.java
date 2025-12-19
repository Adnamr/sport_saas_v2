package com.sportsaas.dashboard.api;

import com.sportsaas.dashboard.api.dto.*;
import com.sportsaas.dashboard.domain.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour le dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "API Dashboard pour statistiques et metriques")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Statistiques globales du dashboard")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/revenue-chart")
    @Operation(summary = "Graphique des revenus")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RevenueChartResponse> getRevenueChart(
            @RequestParam(defaultValue = "last_12_months") String period) {
        return ResponseEntity.ok(dashboardService.getRevenueChart(period));
    }

    @GetMapping("/orders-by-status")
    @Operation(summary = "Repartition des commandes par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrdersByStatusResponse> getOrdersByStatus() {
        return ResponseEntity.ok(dashboardService.getOrdersByStatus());
    }

    @GetMapping("/recent-orders")
    @Operation(summary = "Commandes recentes")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<RecentOrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(Math.min(limit, 50)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Produits en stock bas")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<LowStockItemResponse>> getLowStockItems(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(dashboardService.getLowStockItems(Math.min(limit, 100)));
    }

    @GetMapping("/activity")
    @Operation(summary = "Activites recentes")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ActivityResponse>> getRecentActivities(Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getRecentActivities(pageable));
    }
}
