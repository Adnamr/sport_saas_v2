package com.sportsaas.tenant.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Tenant.
 */
public interface TenantService {

    /**
     * Récupère tous les tenants avec pagination.
     */
    Page<Tenant> findAll(Pageable pageable);

    /**
     * Trouve un tenant par son ID.
     */
    Optional<Tenant> findById(UUID id);

    /**
     * Trouve un tenant par son slug.
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Crée un nouveau tenant.
     */
    Tenant create(Tenant tenant);

    /**
     * Met à jour un tenant existant.
     */
    Tenant update(UUID id, Tenant tenant);

    /**
     * Suspend un tenant.
     */
    Tenant suspend(UUID id);

    /**
     * Réactive un tenant suspendu.
     */
    Tenant activate(UUID id);

    /**
     * Archive un tenant.
     */
    void archive(UUID id);
}
