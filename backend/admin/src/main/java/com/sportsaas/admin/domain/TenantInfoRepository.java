package com.sportsaas.admin.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour les TenantInfos.
 */
@Repository
public interface TenantInfoRepository extends JpaRepository<TenantInfo, UUID> {

    Page<TenantInfo> findByStatusOrderByCreatedAtDesc(TenantStatus status, Pageable pageable);

    Page<TenantInfo> findBySubscriptionPlanOrderByCreatedAtDesc(SubscriptionPlan plan, Pageable pageable);

    List<TenantInfo> findByStatus(TenantStatus status);

    @Query("SELECT t FROM TenantInfo t WHERE t.subscriptionEnd <= :date AND t.status = 'ACTIVE'")
    List<TenantInfo> findExpiringSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT t FROM TenantInfo t WHERE t.subscriptionEnd < :date AND t.status = 'ACTIVE'")
    List<TenantInfo> findExpiredSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM TenantInfo t WHERE t.status = :status")
    long countByStatus(@Param("status") TenantStatus status);

    @Query("SELECT COUNT(t) FROM TenantInfo t WHERE t.subscriptionPlan = :plan")
    long countByPlan(@Param("plan") SubscriptionPlan plan);

    @Query("SELECT t FROM TenantInfo t WHERE " +
           "LOWER(t.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.contactEmail) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<TenantInfo> search(@Param("search") String search, Pageable pageable);
}
