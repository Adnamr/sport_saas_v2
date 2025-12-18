package com.sportsaas.tenant.api;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.api.dto.CreateTenantRequest;
import com.sportsaas.tenant.api.dto.TenantResponse;
import com.sportsaas.tenant.api.dto.UpdateTenantRequest;
import com.sportsaas.tenant.domain.Tenant;
import com.sportsaas.tenant.domain.TenantService;
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

import java.util.UUID;

/**
 * Controller REST pour la gestion des Tenants.
 * Réservé aux Super Admins.
 */
@RestController
@RequestMapping("/tenants")
@Tag(name = "Tenants", description = "Gestion des tenants (Super Admin)")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    @GetMapping
    @Operation(summary = "Lister tous les tenants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponse<TenantResponse>> list(Pageable pageable) {
        Page<Tenant> page = tenantService.findAll(pageable);
        return ResponseEntity.ok(PageResponse.of(page, tenantMapper::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un tenant par ID")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id) {
        Tenant tenant = tenantService.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));
        return ResponseEntity.ok(tenantMapper.toResponse(tenant));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantMapper.toEntity(request);
        Tenant created = tenantService.create(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {
        Tenant existing = tenantService.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));
        tenantMapper.updateEntity(request, existing);
        Tenant updated = tenantService.update(id, existing);
        return ResponseEntity.ok(tenantMapper.toResponse(updated));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspendre un tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> suspend(@PathVariable UUID id) {
        Tenant suspended = tenantService.suspend(id);
        return ResponseEntity.ok(tenantMapper.toResponse(suspended));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Réactiver un tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> activate(@PathVariable UUID id) {
        Tenant activated = tenantService.activate(id);
        return ResponseEntity.ok(tenantMapper.toResponse(activated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archiver un tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        tenantService.archive(id);
        return ResponseEntity.noContent().build();
    }
}
