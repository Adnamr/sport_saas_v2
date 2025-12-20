package com.sportsaas.tenant.infra;

import com.sportsaas.tenant.domain.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtre HTTP pour extraire le tenant depuis le header X-Tenant-ID.
 */
@Slf4j
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantHeader = request.getHeader(TENANT_HEADER);

            if (tenantHeader != null && !tenantHeader.isBlank()) {
                try {
                    UUID tenantId = UUID.fromString(tenantHeader);
                    TenantContext.setCurrentTenant(tenantId);
                    log.debug("Tenant context set: {}", tenantId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid tenant ID format: {}", tenantHeader);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ne pas filtrer les endpoints publics et admin
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api/auth/") ||
               path.startsWith("/tenants");
    }
}
