package com.sportsaas.tenant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Tenants.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Trouve un tenant par son slug.
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Trouve un tenant par son domaine.
     */
    Optional<Tenant> findByDomain(String domain);

    /**
     * Vérifie si un slug existe déjà.
     */
    boolean existsBySlug(String slug);

    /**
     * Vérifie si un domaine existe déjà.
     */
    boolean existsByDomain(String domain);
}
