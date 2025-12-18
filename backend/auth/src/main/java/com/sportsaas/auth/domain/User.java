package com.sportsaas.auth.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entite User - Utilisateur de la plateforme.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends TenantAwareEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    @Column
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column
    private LocalDateTime lastLoginAt;

    /**
     * Retourne le nom complet.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifie si le token de verification email est valide.
     */
    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null
            && emailVerificationTokenExpiresAt != null
            && emailVerificationTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Verifie si le token de reset password est valide.
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null
            && passwordResetTokenExpiresAt != null
            && passwordResetTokenExpiresAt.isAfter(LocalDateTime.now());
    }
}
