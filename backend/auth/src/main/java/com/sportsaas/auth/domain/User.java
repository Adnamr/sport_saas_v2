package com.sportsaas.auth.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column
    private String invitationToken;

    @Column
    private LocalDateTime invitationTokenExpiresAt;

    @Column
    private UUID invitedBy;

    @Column
    private LocalDateTime invitedAt;

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

    /**
     * Verifie si le token d'invitation est valide.
     */
    public boolean isInvitationTokenValid() {
        return invitationToken != null
            && invitationTokenExpiresAt != null
            && invitationTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Verifie si l'utilisateur a ete invite.
     */
    public boolean isInvited() {
        return invitedAt != null;
    }
}
