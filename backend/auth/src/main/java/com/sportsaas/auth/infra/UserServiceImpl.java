package com.sportsaas.auth.infra;

import com.sportsaas.auth.domain.User;
import com.sportsaas.auth.domain.UserRepository;
import com.sportsaas.auth.domain.UserRole;
import com.sportsaas.auth.domain.UserService;
import com.sportsaas.common.event.UserInvitedEvent;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.ForbiddenException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.exception.UnauthorizedException;
import com.sportsaas.tenant.domain.Tenant;
import com.sportsaas.tenant.domain.TenantContext;
import com.sportsaas.tenant.domain.TenantService;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service User.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final String DEFAULT_TENANT_NAME = "Sport SaaS";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantService tenantService;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndTenantId(String email, UUID tenantId) {
        return userRepository.findByEmailAndTenantId(email, tenantId);
    }

    @Override
    @Transactional
    public User create(User user) {
        UUID tenantId = TenantContext.requireCurrentTenant();

        // Verifier unicite email pour ce tenant
        if (userRepository.existsByEmailAndTenantId(user.getEmail(), tenantId)) {
            throw new ConflictException("Un utilisateur avec cet email existe deja");
        }

        user.setTenantId(tenantId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken(generateToken());
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));

        User saved = userRepository.save(user);
        log.info("User created: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User registerPublic(User user) {
        // Verifier que l'email n'existe pas deja (globalement)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Un utilisateur avec cet email existe deja");
        }

        // Creer un nouveau tenant pour l'utilisateur
        String slug = generateSlug(user.getFirstName(), user.getLastName());
        Tenant tenant = new Tenant();
        tenant.setName(user.getFirstName() + " " + user.getLastName());
        tenant.setSlug(slug);
        Tenant savedTenant = tenantService.create(tenant);

        // Creer l'utilisateur comme TENANT_ADMIN
        user.setTenantId(savedTenant.getId());
        user.setRole(UserRole.TENANT_ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true); // Actif immediatement pour simplifier
        user.setEmailVerified(true); // Simplifie - a ameliorer avec verification email

        User saved = userRepository.save(user);
        log.info("Public registration: user {} ({}) with tenant {}", saved.getEmail(), saved.getId(), savedTenant.getSlug());
        return saved;
    }

    private String generateSlug(String firstName, String lastName) {
        String base = (firstName + "-" + lastName)
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public User update(UUID id, User user) {
        User existing = findByIdOrThrow(id);

        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());

        User saved = userRepository.save(existing);
        log.info("User updated: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User enable(UUID id) {
        User user = findByIdOrThrow(id);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        log.info("User enabled: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User disable(UUID id, UUID currentUserId) {
        User user = findByIdOrThrow(id);

        // Empecher de se desactiver soi-meme
        if (user.getId().equals(currentUserId)) {
            throw new ForbiddenException("Vous ne pouvez pas desactiver votre propre compte");
        }

        // Verifier si c'est le dernier admin actif du tenant
        if (user.getRole() == UserRole.TENANT_ADMIN && user.isEnabled()) {
            long enabledAdminCount = userRepository.countEnabledByRole(UserRole.TENANT_ADMIN);
            if (enabledAdminCount <= 1) {
                throw new ConflictException("Impossible de desactiver le dernier administrateur actif du tenant");
            }
        }

        user.setEnabled(false);
        User saved = userRepository.save(user);
        log.info("User disabled: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new NotFoundException("Token de verification invalide"));

        if (!user.isEmailVerificationTokenValid()) {
            throw new UnauthorizedException("Le token de verification a expire");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);

        User saved = userRepository.save(user);
        log.info("Email verified for user: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordResetToken(generateToken());
            user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
            userRepository.save(user);
            log.info("Password reset requested for: {}", email);
            // TODO: Envoyer email avec le token
        });
        // Ne pas reveler si l'email existe ou non
    }

    @Override
    @Transactional
    public User resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new NotFoundException("Token de reset invalide"));

        if (!user.isPasswordResetTokenValid()) {
            throw new UnauthorizedException("Le token de reset a expire");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        User saved = userRepository.save(user);
        log.info("Password reset for user: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User changePassword(UUID id, String oldPassword, String newPassword) {
        User user = findByIdOrThrow(id);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UnauthorizedException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        log.info("Password changed for user: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public User authenticate(String email, String password, UUID tenantId) {
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Compte desactive");
        }

        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User authenticateByEmail(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Compte desactive");
        }

        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        User user = findByIdOrThrow(id);

        // Empecher de se supprimer soi-meme
        if (user.getId().equals(currentUserId)) {
            throw new ForbiddenException("Vous ne pouvez pas supprimer votre propre compte");
        }

        // Empecher de supprimer un SUPER_ADMIN
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Impossible de supprimer un super administrateur");
        }

        // Verifier si c'est le dernier admin du tenant
        if (user.getRole() == UserRole.TENANT_ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.TENANT_ADMIN);
            if (adminCount <= 1) {
                throw new ConflictException("Impossible de supprimer le dernier administrateur du tenant");
            }
        }

        userRepository.delete(user);
        log.info("User deleted: {} ({})", user.getEmail(), user.getId());
    }

    @Override
    public Page<User> findByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Override
    @Transactional
    public User updateRole(UUID id, UserRole role, UUID currentUserId, UserRole currentUserRole) {
        User user = findByIdOrThrow(id);

        // Seul un SUPER_ADMIN peut promouvoir en SUPER_ADMIN
        if (role == UserRole.SUPER_ADMIN && currentUserRole != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Seul un super administrateur peut promouvoir en super administrateur");
        }

        // Empecher de modifier le role d'un SUPER_ADMIN (sauf par un autre SUPER_ADMIN)
        if (user.getRole() == UserRole.SUPER_ADMIN && currentUserRole != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Impossible de modifier le role d'un super administrateur");
        }

        // Empecher de se retrograder soi-meme
        if (user.getId().equals(currentUserId) && role.ordinal() > user.getRole().ordinal()) {
            throw new ForbiddenException("Vous ne pouvez pas vous retrograder vous-meme");
        }

        // Verifier qu'on ne supprime pas le dernier TENANT_ADMIN
        if (user.getRole() == UserRole.TENANT_ADMIN && role != UserRole.TENANT_ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.TENANT_ADMIN);
            if (adminCount <= 1) {
                throw new ConflictException("Impossible de retrograder le dernier administrateur du tenant");
            }
        }

        UserRole oldRole = user.getRole();
        user.setRole(role);
        User saved = userRepository.save(user);
        log.info("User role updated: {} ({}) from {} to {}", saved.getEmail(), saved.getId(), oldRole, role);
        return saved;
    }

    @Override
    @Transactional
    public User inviteUser(String email, String firstName, String lastName, UserRole role, UserRole currentUserRole) {
        UUID tenantId = TenantContext.requireCurrentTenant();

        // Seul un SUPER_ADMIN peut inviter un SUPER_ADMIN
        if (role == UserRole.SUPER_ADMIN && currentUserRole != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Seul un super administrateur peut inviter un super administrateur");
        }

        // Verifier unicite email pour ce tenant
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new ConflictException("Un utilisateur avec cet email existe deja");
        }

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Mot de passe temporaire
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setInvitationToken(generateToken());
        user.setInvitationTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS * 7)); // 7 jours
        user.setInvitedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.info("User invited: {} ({}) with role {}", saved.getEmail(), saved.getId(), role);

        // Publier l'evenement pour l'envoi de l'email
        eventPublisher.publishEvent(new UserInvitedEvent(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getInvitationToken(),
                DEFAULT_TENANT_NAME,
                tenantId
        ));

        return saved;
    }

    @Override
    @Transactional
    public User resendInvitation(UUID id) {
        User user = findByIdOrThrow(id);

        if (!user.isInvited()) {
            throw new ConflictException("Cet utilisateur n'a pas ete invite");
        }

        if (user.isEnabled()) {
            throw new ConflictException("Cet utilisateur a deja active son compte");
        }

        user.setInvitationToken(generateToken());
        user.setInvitationTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS * 7));

        User saved = userRepository.save(user);
        log.info("Invitation resent for user: {} ({})", saved.getEmail(), saved.getId());

        // Publier l'evenement pour l'envoi de l'email
        eventPublisher.publishEvent(new UserInvitedEvent(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getInvitationToken(),
                DEFAULT_TENANT_NAME,
                saved.getTenantId()
        ));

        return saved;
    }

    @Override
    @Transactional
    public User completeInvitation(String token, String password) {
        User user = userRepository.findByInvitationToken(token)
                .orElseThrow(() -> new NotFoundException("Token d'invitation invalide"));

        if (!user.isInvitationTokenValid()) {
            throw new UnauthorizedException("Le token d'invitation a expire");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setInvitationToken(null);
        user.setInvitationTokenExpiresAt(null);

        User saved = userRepository.save(user);
        log.info("Invitation completed for user: {} ({})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    private User findByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
