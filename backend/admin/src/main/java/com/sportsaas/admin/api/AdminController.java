package com.sportsaas.admin.api;

import com.sportsaas.admin.api.dto.*;
import com.sportsaas.admin.domain.*;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Gestion de la plateforme (SUPER_ADMIN)")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;

    // ========== Gestion des tenants ==========

    @PostMapping("/tenants")
    @Operation(summary = "Creer un nouveau tenant")
    public ResponseEntity<TenantInfoResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantInfo tenantInfo = adminMapper.toTenantInfo(request);
        if (request.subscriptionPlan() != null) {
            tenantInfo.setSubscriptionPlan(request.subscriptionPlan());
        }
        if (request.subscriptionEnd() != null) {
            tenantInfo.setSubscriptionEnd(request.subscriptionEnd());
        }
        if (request.maxUsers() != null) {
            tenantInfo.setMaxUsers(request.maxUsers());
        }
        if (request.maxProducts() != null) {
            tenantInfo.setMaxProducts(request.maxProducts());
        }
        if (request.maxStorageMb() != null) {
            tenantInfo.setMaxStorageMb(request.maxStorageMb());
        }

        TenantInfo created = adminService.createTenant(tenantInfo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminMapper.toTenantInfoResponse(created));
    }

    @GetMapping("/tenants/{id}")
    @Operation(summary = "Recuperer un tenant")
    public ResponseEntity<TenantInfoResponse> getTenant(@PathVariable UUID id) {
        TenantInfo tenant = adminService.findTenantById(id)
                .orElseThrow(() -> new NotFoundException("TenantInfo", id));
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(tenant));
    }

    @GetMapping("/tenants")
    @Operation(summary = "Lister tous les tenants")
    public ResponseEntity<PageResponse<TenantInfoResponse>> listTenants(Pageable pageable) {
        Page<TenantInfo> page = adminService.findAllTenants(pageable);
        return ResponseEntity.ok(PageResponse.of(page, adminMapper::toTenantInfoResponse));
    }

    @GetMapping("/tenants/status/{status}")
    @Operation(summary = "Lister les tenants par statut")
    public ResponseEntity<PageResponse<TenantInfoResponse>> listTenantsByStatus(
            @PathVariable TenantStatus status,
            Pageable pageable) {
        Page<TenantInfo> page = adminService.findTenantsByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, adminMapper::toTenantInfoResponse));
    }

    @GetMapping("/tenants/plan/{plan}")
    @Operation(summary = "Lister les tenants par plan")
    public ResponseEntity<PageResponse<TenantInfoResponse>> listTenantsByPlan(
            @PathVariable SubscriptionPlan plan,
            Pageable pageable) {
        Page<TenantInfo> page = adminService.findTenantsByPlan(plan, pageable);
        return ResponseEntity.ok(PageResponse.of(page, adminMapper::toTenantInfoResponse));
    }

    @GetMapping("/tenants/search")
    @Operation(summary = "Rechercher des tenants")
    public ResponseEntity<PageResponse<TenantInfoResponse>> searchTenants(
            @RequestParam String q,
            Pageable pageable) {
        Page<TenantInfo> page = adminService.searchTenants(q, pageable);
        return ResponseEntity.ok(PageResponse.of(page, adminMapper::toTenantInfoResponse));
    }

    @PutMapping("/tenants/{id}")
    @Operation(summary = "Mettre a jour un tenant")
    public ResponseEntity<TenantInfoResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {
        TenantInfo updates = adminMapper.toTenantInfo(request);
        TenantInfo updated = adminService.updateTenant(id, updates);
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(updated));
    }

    @PostMapping("/tenants/{id}/activate")
    @Operation(summary = "Activer un tenant")
    public ResponseEntity<TenantInfoResponse> activateTenant(@PathVariable UUID id) {
        TenantInfo activated = adminService.activateTenant(id);
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(activated));
    }

    @PostMapping("/tenants/{id}/suspend")
    @Operation(summary = "Suspendre un tenant")
    public ResponseEntity<TenantInfoResponse> suspendTenant(
            @PathVariable UUID id,
            @Valid @RequestBody SuspendRequest request) {
        TenantInfo suspended = adminService.suspendTenant(id, request.reason());
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(suspended));
    }

    @PostMapping("/tenants/{id}/deactivate")
    @Operation(summary = "Desactiver un tenant")
    public ResponseEntity<TenantInfoResponse> deactivateTenant(@PathVariable UUID id) {
        TenantInfo deactivated = adminService.deactivateTenant(id);
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(deactivated));
    }

    @PostMapping("/tenants/{id}/change-plan")
    @Operation(summary = "Changer le plan d'abonnement")
    public ResponseEntity<TenantInfoResponse> changePlan(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePlanRequest request) {
        TenantInfo updated = adminService.changePlan(id, request.plan());
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(updated));
    }

    @PostMapping("/tenants/{id}/limits")
    @Operation(summary = "Mettre a jour les limites")
    public ResponseEntity<TenantInfoResponse> updateLimits(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLimitsRequest request) {
        TenantInfo updated = adminService.updateLimits(id, request.maxUsers(), request.maxProducts(), request.maxStorageMb());
        return ResponseEntity.ok(adminMapper.toTenantInfoResponse(updated));
    }

    // ========== Statistiques plateforme ==========

    @GetMapping("/stats")
    @Operation(summary = "Statistiques de la plateforme")
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(adminMapper.toPlatformStatsResponse(adminService.getPlatformStats()));
    }

    @GetMapping("/stats/by-plan")
    @Operation(summary = "Statistiques par plan")
    public ResponseEntity<List<PlanStatsResponse>> getStatsByPlan() {
        return ResponseEntity.ok(adminMapper.toPlanStatsResponseList(adminService.getStatsByPlan()));
    }

    // ========== Maintenance ==========

    @PostMapping("/maintenance/process-expired")
    @Operation(summary = "Traiter les abonnements expires")
    public ResponseEntity<Integer> processExpiredSubscriptions() {
        int count = adminService.processExpiredSubscriptions();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/maintenance/send-reminders")
    @Operation(summary = "Envoyer des rappels d'expiration")
    public ResponseEntity<Integer> sendExpirationReminders(
            @RequestParam(defaultValue = "7") int daysBeforeExpiry) {
        int count = adminService.sendExpirationReminders(daysBeforeExpiry);
        return ResponseEntity.ok(count);
    }
}
