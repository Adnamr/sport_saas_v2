package com.sportsaas.admin.infra;

import com.sportsaas.admin.domain.*;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation du service d'administration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final TenantInfoRepository tenantInfoRepository;

    @Override
    @Transactional
    public TenantInfo createTenant(TenantInfo tenantInfo) {
        if (tenantInfo.getId() == null) {
            tenantInfo.setId(UUID.randomUUID());
        }
        tenantInfo.setStatus(TenantStatus.PENDING_ACTIVATION);
        tenantInfo.setSubscriptionStart(LocalDate.now());

        TenantInfo saved = tenantInfoRepository.save(tenantInfo);
        log.info("Tenant created: {} ({})", saved.getCompanyName(), saved.getId());
        return saved;
    }

    @Override
    public Optional<TenantInfo> findTenantById(UUID id) {
        return tenantInfoRepository.findById(id);
    }

    @Override
    public Page<TenantInfo> findAllTenants(Pageable pageable) {
        return tenantInfoRepository.findAll(pageable);
    }

    @Override
    public Page<TenantInfo> findTenantsByStatus(TenantStatus status, Pageable pageable) {
        return tenantInfoRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<TenantInfo> findTenantsByPlan(SubscriptionPlan plan, Pageable pageable) {
        return tenantInfoRepository.findBySubscriptionPlanOrderByCreatedAtDesc(plan, pageable);
    }

    @Override
    public Page<TenantInfo> searchTenants(String query, Pageable pageable) {
        return tenantInfoRepository.search(query, pageable);
    }

    @Override
    @Transactional
    public TenantInfo updateTenant(UUID id, TenantInfo updates) {
        TenantInfo tenant = findByIdOrThrow(id);

        if (updates.getCompanyName() != null) {
            tenant.setCompanyName(updates.getCompanyName());
        }
        if (updates.getContactEmail() != null) {
            tenant.setContactEmail(updates.getContactEmail());
        }
        if (updates.getContactPhone() != null) {
            tenant.setContactPhone(updates.getContactPhone());
        }
        if (updates.getAddress() != null) {
            tenant.setAddress(updates.getAddress());
        }
        if (updates.getNotes() != null) {
            tenant.setNotes(updates.getNotes());
        }

        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant updated: {}", id);
        return saved;
    }

    @Override
    @Transactional
    public TenantInfo activateTenant(UUID id) {
        TenantInfo tenant = findByIdOrThrow(id);

        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            throw new BadRequestException("Ce tenant est deja actif");
        }

        tenant.activate();
        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant activated: {} ({})", tenant.getCompanyName(), id);
        return saved;
    }

    @Override
    @Transactional
    public TenantInfo suspendTenant(UUID id, String reason) {
        TenantInfo tenant = findByIdOrThrow(id);

        if (tenant.getStatus() == TenantStatus.SUSPENDED) {
            throw new BadRequestException("Ce tenant est deja suspendu");
        }

        tenant.suspend(reason);
        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant suspended: {} ({}) - reason: {}", tenant.getCompanyName(), id, reason);
        return saved;
    }

    @Override
    @Transactional
    public TenantInfo deactivateTenant(UUID id) {
        TenantInfo tenant = findByIdOrThrow(id);

        if (tenant.getStatus() == TenantStatus.INACTIVE) {
            throw new BadRequestException("Ce tenant est deja inactif");
        }

        tenant.deactivate();
        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant deactivated: {} ({})", tenant.getCompanyName(), id);
        return saved;
    }

    @Override
    @Transactional
    public TenantInfo changePlan(UUID id, SubscriptionPlan plan) {
        TenantInfo tenant = findByIdOrThrow(id);

        SubscriptionPlan oldPlan = tenant.getSubscriptionPlan();
        tenant.setSubscriptionPlan(plan);

        // Ajuster les limites selon le plan
        switch (plan) {
            case FREE:
                tenant.setMaxUsers(3);
                tenant.setMaxProducts(50);
                tenant.setMaxStorageMb(100);
                break;
            case STARTER:
                tenant.setMaxUsers(10);
                tenant.setMaxProducts(500);
                tenant.setMaxStorageMb(1000);
                break;
            case PROFESSIONAL:
                tenant.setMaxUsers(50);
                tenant.setMaxProducts(5000);
                tenant.setMaxStorageMb(10000);
                break;
            case ENTERPRISE:
            case CUSTOM:
                // Pas de limites par defaut pour enterprise/custom
                tenant.setMaxUsers(null);
                tenant.setMaxProducts(null);
                tenant.setMaxStorageMb(null);
                break;
        }

        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant plan changed: {} ({}) from {} to {}", tenant.getCompanyName(), id, oldPlan, plan);
        return saved;
    }

    @Override
    @Transactional
    public TenantInfo updateLimits(UUID id, Integer maxUsers, Integer maxProducts, Integer maxStorageMb) {
        TenantInfo tenant = findByIdOrThrow(id);

        if (maxUsers != null) {
            tenant.setMaxUsers(maxUsers);
        }
        if (maxProducts != null) {
            tenant.setMaxProducts(maxProducts);
        }
        if (maxStorageMb != null) {
            tenant.setMaxStorageMb(maxStorageMb);
        }

        TenantInfo saved = tenantInfoRepository.save(tenant);
        log.info("Tenant limits updated: {}", id);
        return saved;
    }

    @Override
    public PlatformStats getPlatformStats() {
        long totalTenants = tenantInfoRepository.count();
        long activeTenants = tenantInfoRepository.countByStatus(TenantStatus.ACTIVE);
        long suspendedTenants = tenantInfoRepository.countByStatus(TenantStatus.SUSPENDED);
        long trialTenants = tenantInfoRepository.countByStatus(TenantStatus.TRIAL);

        // Note: Pour totalUsers, totalProducts, totalOrders, totalRentals,
        // il faudrait injecter les repositories correspondants ou utiliser des queries cross-tenant
        // Pour l'instant, on retourne 0
        return new PlatformStats(
                totalTenants,
                activeTenants,
                suspendedTenants,
                trialTenants,
                0, // totalUsers - a implementer
                0, // totalProducts - a implementer
                0, // totalOrders - a implementer
                0  // totalRentals - a implementer
        );
    }

    @Override
    public List<PlanStats> getStatsByPlan() {
        return List.of(
                new PlanStats(SubscriptionPlan.FREE, tenantInfoRepository.countByPlan(SubscriptionPlan.FREE)),
                new PlanStats(SubscriptionPlan.STARTER, tenantInfoRepository.countByPlan(SubscriptionPlan.STARTER)),
                new PlanStats(SubscriptionPlan.PROFESSIONAL, tenantInfoRepository.countByPlan(SubscriptionPlan.PROFESSIONAL)),
                new PlanStats(SubscriptionPlan.ENTERPRISE, tenantInfoRepository.countByPlan(SubscriptionPlan.ENTERPRISE)),
                new PlanStats(SubscriptionPlan.CUSTOM, tenantInfoRepository.countByPlan(SubscriptionPlan.CUSTOM))
        );
    }

    @Override
    @Transactional
    public int processExpiredSubscriptions() {
        List<TenantInfo> expired = tenantInfoRepository.findExpiredSubscriptions(LocalDate.now());
        int count = 0;

        for (TenantInfo tenant : expired) {
            try {
                tenant.suspend("Abonnement expire");
                tenantInfoRepository.save(tenant);
                count++;
                log.info("Tenant suspended due to expired subscription: {}", tenant.getId());
            } catch (Exception e) {
                log.error("Failed to suspend tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }

        log.info("Processed {} expired subscriptions", count);
        return count;
    }

    @Override
    public int sendExpirationReminders(int daysBeforeExpiry) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysBeforeExpiry);
        List<TenantInfo> expiring = tenantInfoRepository.findExpiringSubscriptions(expiryDate);

        // Note: Ici on devrait envoyer des emails via NotificationService
        // Pour l'instant, on log seulement
        for (TenantInfo tenant : expiring) {
            log.info("Subscription expiring soon for tenant: {} (expires: {})",
                    tenant.getCompanyName(), tenant.getSubscriptionEnd());
        }

        log.info("Found {} tenants with expiring subscriptions", expiring.size());
        return expiring.size();
    }

    private TenantInfo findByIdOrThrow(UUID id) {
        return tenantInfoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TenantInfo", id));
    }
}
