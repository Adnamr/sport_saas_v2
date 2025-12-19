package com.sportsaas.dashboard.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour les Activities.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    /**
     * Trouve les activites par type.
     */
    Page<Activity> findByTypeOrderByCreatedAtDesc(ActivityType type, Pageable pageable);

    /**
     * Trouve les activites d'un utilisateur.
     */
    Page<Activity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Trouve les activites liees a une entite.
     */
    List<Activity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    /**
     * Trouve les activites recentes.
     */
    @Query("SELECT a FROM Activity a ORDER BY a.createdAt DESC")
    Page<Activity> findRecentActivities(Pageable pageable);

    /**
     * Trouve les activites dans une periode.
     */
    @Query("SELECT a FROM Activity a WHERE a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<Activity> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Compte les activites par type dans une periode.
     */
    @Query("SELECT a.type, COUNT(a) FROM Activity a WHERE a.createdAt BETWEEN :start AND :end GROUP BY a.type")
    List<Object[]> countByTypeInDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Trouve les activites par types.
     */
    @Query("SELECT a FROM Activity a WHERE a.type IN :types ORDER BY a.createdAt DESC")
    Page<Activity> findByTypeIn(@Param("types") List<ActivityType> types, Pageable pageable);
}
