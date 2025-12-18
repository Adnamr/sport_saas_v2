package com.sportsaas.tenant.api;

import com.sportsaas.tenant.api.dto.CreateTenantRequest;
import com.sportsaas.tenant.api.dto.TenantResponse;
import com.sportsaas.tenant.api.dto.UpdateTenantRequest;
import com.sportsaas.tenant.domain.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct pour les Tenants.
 */
@Mapper(componentModel = "spring")
public interface TenantMapper {

    TenantResponse toResponse(Tenant tenant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tenant toEntity(CreateTenantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateTenantRequest request, @MappingTarget Tenant tenant);
}
