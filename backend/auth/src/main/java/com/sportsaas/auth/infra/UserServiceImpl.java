package com.sportsaas.auth.infra;

import com.sportsaas.auth.domain.User;
import com.sportsaas.auth.domain.UserRepository;
import com.sportsaas.auth.domain.UserService;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.exception.UnauthorizedException;
import com.sportsaas.tenant.domain.TenantContext;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    public User disable(UUID id) {
        User user = findByIdOrThrow(id);
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

    private User findByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
