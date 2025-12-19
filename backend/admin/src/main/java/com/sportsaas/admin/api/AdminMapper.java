package com.sportsaas.admin.api;

import com.sportsaas.admin.api.dto.*;
import com.sportsaas.admin.domain.AdminService;
import com.sportsaas.admin.domain.TenantInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "subscriptionExpired", expression = "java(tenantInfo.isSubscriptionExpired())")
    TenantInfoResponse toTenantInfoResponse(TenantInfo tenantInfo);

    List<TenantInfoResponse> toTenantInfoResponseList(List<TenantInfo> tenantInfos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "usedStorageMb", ignore = true)
    @Mapping(target = "activatedAt", ignore = true)
    @Mapping(target = "suspendedAt", ignore = true)
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionStart", ignore = true)
    TenantInfo toTenantInfo(CreateTenantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "subscriptionStart", ignore = true)
    @Mapping(target = "subscriptionEnd", ignore = true)
    @Mapping(target = "maxUsers", ignore = true)
    @Mapping(target = "maxProducts", ignore = true)
    @Mapping(target = "maxStorageMb", ignore = true)
    @Mapping(target = "usedStorageMb", ignore = true)
    @Mapping(target = "activatedAt", ignore = true)
    @Mapping(target = "suspendedAt", ignore = true)
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TenantInfo toTenantInfo(UpdateTenantRequest request);

    default PlatformStatsResponse toPlatformStatsResponse(AdminService.PlatformStats stats) {
        return new PlatformStatsResponse(
                stats.totalTenants(),
                stats.activeTenants(),
                stats.suspendedTenants(),
                stats.trialTenants(),
                stats.totalUsers(),
                stats.totalProducts(),
                stats.totalOrders(),
                stats.totalRentals()
        );
    }

    default PlanStatsResponse toPlanStatsResponse(AdminService.PlanStats stats) {
        return new PlanStatsResponse(stats.plan(), stats.count());
    }

    default List<PlanStatsResponse> toPlanStatsResponseList(List<AdminService.PlanStats> stats) {
        return stats.stream().map(this::toPlanStatsResponse).toList();
    }
}
