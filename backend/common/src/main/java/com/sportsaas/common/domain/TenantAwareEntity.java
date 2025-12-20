package com.sportsaas.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Entité de base tenant-aware.
 * Toutes les entités multi-tenant doivent hériter de cette classe.
 * Le filtre Hibernate assure que seules les données du tenant courant sont accessibles.
 */
@MappedSuperclass
@Getter
@Setter
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = UUID.class),
    defaultCondition = "tenant_id = :tenantId"
)
@Filter(name = "tenantFilter")
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
}
