package com.sportsaas.auth.api;

import com.sportsaas.auth.api.dto.*;
import com.sportsaas.auth.domain.User;
import com.sportsaas.auth.domain.UserRole;
import com.sportsaas.auth.domain.UserService;
import com.sportsaas.auth.infra.JwtService;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.exception.UnauthorizedException;
import com.sportsaas.tenant.domain.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller REST pour l'authentification.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints d'authentification")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UUID tenantId = TenantContext.requireCurrentTenant();

        User user = userService.authenticate(request.getEmail(), request.getPassword(), tenantId);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.of(
                accessToken,
                refreshToken,
                jwtExpiration / 1000,
                authMapper.toUserResponse(user)
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription utilisateur")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.CUSTOMER);

        User created = userService.create(user);

        String accessToken = jwtService.generateToken(created);
        String refreshToken = jwtService.generateRefreshToken(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(
                accessToken,
                refreshToken,
                jwtExpiration / 1000,
                authMapper.toUserResponse(created)
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraichir le token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtService.isTokenValid(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token invalide ou expire");
        }

        UUID userId = jwtService.extractUserId(request.getRefreshToken());
        User user = userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Compte desactive");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.of(
                accessToken,
                refreshToken,
                jwtExpiration / 1000,
                authMapper.toUserResponse(user)
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Recuperer l'utilisateur courant")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UUID userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        return ResponseEntity.ok(authMapper.toUserResponse(user));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de reset password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de reinitialisation a ete envoye"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password avec token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Mot de passe reinitialise avec succes"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verifier l'email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verifie avec succes"));
    }
}
