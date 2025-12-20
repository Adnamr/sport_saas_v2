package com.sportsaas.tenant.infra;

import com.sportsaas.tenant.domain.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspect pour activer automatiquement le filtre Hibernate tenant
 * sur toutes les requêtes de repository.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantHibernateFilter {

    public static final String TENANT_FILTER_NAME = "tenantFilter";
    public static final String TENANT_PARAMETER = "tenantId";

    private final EntityManager entityManager;

    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void enableTenantFilter() {
        if (TenantContext.hasTenant()) {
            Session session = entityManager.unwrap(Session.class);
            org.hibernate.Filter filter = session.enableFilter(TENANT_FILTER_NAME);
            filter.setParameter(TENANT_PARAMETER, TenantContext.getCurrentTenant());
            log.trace("Tenant filter enabled for tenant: {}", TenantContext.getCurrentTenant());
        }
    }
}
