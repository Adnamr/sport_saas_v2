package com.sportsaas.auth.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Trouve un utilisateur par email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par email et tenant.
     */
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    /**
     * Trouve un utilisateur par token de verification email.
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Trouve un utilisateur par token de reset password.
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Trouve un utilisateur par token d'invitation.
     */
    Optional<User> findByInvitationToken(String token);

    /**
     * Verifie si un email existe pour un tenant.
     */
    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    /**
     * Trouve les utilisateurs par role.
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Compte les utilisateurs par role.
     */
    long countByRole(UserRole role);

    /**
     * Compte les utilisateurs actifs par role.
     */
    long countByRoleAndEnabledTrue(UserRole role);

    /**
     * Alias pour countByRoleAndEnabledTrue.
     */
    default long countEnabledByRole(UserRole role) {
        return countByRoleAndEnabledTrue(role);
    }

    /**
     * Compte les utilisateurs par role crees dans une periode.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt BETWEEN :start AND :end")
    long countByRoleAndCreatedAtBetween(
        @Param("role") UserRole role,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
