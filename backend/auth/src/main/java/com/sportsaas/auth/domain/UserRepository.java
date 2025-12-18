package com.sportsaas.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Verifie si un email existe pour un tenant.
     */
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}
