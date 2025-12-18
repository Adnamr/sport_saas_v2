package com.sportsaas.tenant.infra;

import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.util.StringUtils;
import com.sportsaas.tenant.domain.Tenant;
import com.sportsaas.tenant.domain.TenantRepository;
import com.sportsaas.tenant.domain.TenantService;
import com.sportsaas.tenant.domain.TenantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service Tenant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public Page<Tenant> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Tenant create(Tenant tenant) {
        // Générer le slug si non fourni
        if (tenant.getSlug() == null || tenant.getSlug().isBlank()) {
            tenant.setSlug(StringUtils.slugify(tenant.getName()));
        }

        // Vérifier l'unicité du slug
        if (tenantRepository.existsBySlug(tenant.getSlug())) {
            throw new ConflictException("Un tenant avec le slug '" + tenant.getSlug() + "' existe déjà");
        }

        // Vérifier l'unicité du domaine si fourni
        if (tenant.getDomain() != null && !tenant.getDomain().isBlank()) {
            if (tenantRepository.existsByDomain(tenant.getDomain())) {
                throw new ConflictException("Un tenant avec le domaine '" + tenant.getDomain() + "' existe déjà");
            }
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant créé: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Tenant update(UUID id, Tenant tenant) {
        Tenant existing = findByIdOrThrow(id);

        // Vérifier l'unicité du domaine si modifié
        if (tenant.getDomain() != null && !tenant.getDomain().isBlank()) {
            if (!tenant.getDomain().equals(existing.getDomain())
                    && tenantRepository.existsByDomain(tenant.getDomain())) {
                throw new ConflictException("Un tenant avec le domaine '" + tenant.getDomain() + "' existe déjà");
            }
        }

        existing.setName(tenant.getName());
        existing.setDomain(tenant.getDomain());
        existing.setSettings(tenant.getSettings());

        Tenant saved = tenantRepository.save(existing);
        log.info("Tenant mis à jour: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Tenant suspend(UUID id) {
        Tenant tenant = findByIdOrThrow(id);
        tenant.setStatus(TenantStatus.SUSPENDED);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant suspendu: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Tenant activate(UUID id) {
        Tenant tenant = findByIdOrThrow(id);
        tenant.setStatus(TenantStatus.ACTIVE);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant réactivé: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        Tenant tenant = findByIdOrThrow(id);
        tenant.setStatus(TenantStatus.ARCHIVED);
        tenantRepository.save(tenant);
        log.info("Tenant archivé: {} ({})", tenant.getName(), tenant.getId());
    }

    private Tenant findByIdOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));
    }
}
