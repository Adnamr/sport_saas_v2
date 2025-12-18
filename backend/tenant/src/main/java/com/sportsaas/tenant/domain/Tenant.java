package com.sportsaas.tenant.domain;

import com.sportsaas.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité Tenant - Représente un locataire de la plateforme.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(columnDefinition = "jsonb")
    private String settings;

    /**
     * Vérifie si le tenant est actif.
     */
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
}
