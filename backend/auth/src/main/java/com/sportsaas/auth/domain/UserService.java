package com.sportsaas.auth.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service User.
 */
public interface UserService {

    /**
     * Recupere tous les utilisateurs avec pagination.
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Trouve un utilisateur par son ID.
     */
    Optional<User> findById(UUID id);

    /**
     * Trouve un utilisateur par email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par email et tenant.
     */
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    /**
     * Cree un nouvel utilisateur.
     */
    User create(User user);

    /**
     * Met a jour un utilisateur.
     */
    User update(UUID id, User user);

    /**
     * Active un utilisateur.
     */
    User enable(UUID id);

    /**
     * Desactive un utilisateur.
     */
    User disable(UUID id);

    /**
     * Verifie l'email d'un utilisateur.
     */
    User verifyEmail(String token);

    /**
     * Demande de reset password.
     */
    void requestPasswordReset(String email);

    /**
     * Reset le password.
     */
    User resetPassword(String token, String newPassword);

    /**
     * Change le password.
     */
    User changePassword(UUID id, String oldPassword, String newPassword);

    /**
     * Authentifie un utilisateur et met a jour lastLoginAt.
     */
    User authenticate(String email, String password, UUID tenantId);
}
