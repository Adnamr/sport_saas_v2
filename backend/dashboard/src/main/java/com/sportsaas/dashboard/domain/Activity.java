package com.sportsaas.dashboard.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entite Activity - Journal d'activites.
 */
@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activities_type", columnList = "type"),
    @Index(name = "idx_activities_user", columnList = "user_id"),
    @Index(name = "idx_activities_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_activities_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    /** Description lisible de l'activite */
    @Column(nullable = false)
    private String description;

    /** ID de l'utilisateur ayant effectue l'action */
    @Column(name = "user_id")
    private UUID userId;

    /** Nom de l'utilisateur (denormalise pour affichage) */
    @Column(name = "user_name")
    private String userName;

    /** Type de l'entite concernee (ex: "Order", "Product") */
    @Column(name = "entity_type")
    private String entityType;

    /** ID de l'entite concernee */
    @Column(name = "entity_id")
    private UUID entityId;

    /** Donnees additionnelles en JSON */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Adresse IP de l'utilisateur */
    @Column(name = "ip_address")
    private String ipAddress;

    /** User-Agent du navigateur */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Constructeur pratique pour creer une activite simple.
     */
    public Activity(ActivityType type, String description, UUID userId, String userName) {
        this.type = type;
        this.description = description;
        this.userId = userId;
        this.userName = userName;
    }

    /**
     * Constructeur pratique pour creer une activite liee a une entite.
     */
    public Activity(ActivityType type, String description, UUID userId, String userName,
                    String entityType, UUID entityId) {
        this.type = type;
        this.description = description;
        this.userId = userId;
        this.userName = userName;
        this.entityType = entityType;
        this.entityId = entityId;
    }
}
