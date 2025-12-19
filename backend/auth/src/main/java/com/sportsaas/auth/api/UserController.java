package com.sportsaas.auth.api;

import com.sportsaas.auth.api.dto.*;
import com.sportsaas.auth.domain.User;
import com.sportsaas.auth.domain.UserRole;
import com.sportsaas.auth.domain.UserService;
import com.sportsaas.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller REST pour la gestion des utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestion des utilisateurs")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthMapper authMapper;

    @GetMapping
    @Operation(summary = "Liste des utilisateurs")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        Page<User> users = userService.findAll(pageable);
        return ResponseEntity.ok(users.map(authMapper::toUserResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un utilisateur par ID")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN') or #id == authentication.principal")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return ResponseEntity.ok(authMapper.toUserResponse(user));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Liste des utilisateurs par role")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserResponse>> listByRole(
            @PathVariable UserRole role,
            Pageable pageable) {
        Page<User> users = userService.findByRole(role, pageable);
        return ResponseEntity.ok(users.map(authMapper::toUserResponse));
    }

    @PostMapping
    @Operation(summary = "Creer un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());

        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toUserResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN') or #id == authentication.principal")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User updated = userService.update(id, user);
        return ResponseEntity.ok(authMapper.toUserResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId) {
        userService.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invite")
    @Operation(summary = "Inviter un utilisateur par email")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> invite(
            @Valid @RequestBody InviteUserRequest request,
            Authentication authentication) {
        UserRole currentUserRole = extractRole(authentication);
        User invited = userService.inviteUser(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.role(),
                currentUserRole
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toUserResponse(invited));
    }

    @PostMapping("/{id}/resend-invitation")
    @Operation(summary = "Renvoyer l'invitation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> resendInvitation(@PathVariable UUID id) {
        User user = userService.resendInvitation(id);
        return ResponseEntity.ok(authMapper.toUserResponse(user));
    }

    @PostMapping("/complete-invitation")
    @Operation(summary = "Completer l'invitation (definir le mot de passe)")
    public ResponseEntity<Map<String, String>> completeInvitation(
            @Valid @RequestBody CompleteInvitationRequest request) {
        userService.completeInvitation(request.token(), request.password());
        return ResponseEntity.ok(Map.of("message", "Compte active avec succes"));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Changer le role d'un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        UserRole currentUserRole = extractRole(authentication);
        User updated = userService.updateRole(id, request.role(), currentUserId, currentUserRole);
        return ResponseEntity.ok(authMapper.toUserResponse(updated));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Activer un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> enable(@PathVariable UUID id) {
        User enabled = userService.enable(id);
        return ResponseEntity.ok(authMapper.toUserResponse(enabled));
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Desactiver un utilisateur")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> disable(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId) {
        User disabled = userService.disable(id, currentUserId);
        return ResponseEntity.ok(authMapper.toUserResponse(disabled));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Changer son mot de passe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Mot de passe change avec succes"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des utilisateurs")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserStatsResponse> getStats() {
        return ResponseEntity.ok(new UserStatsResponse(
                userService.count(),
                userService.countByRole(UserRole.TENANT_ADMIN),
                userService.countByRole(UserRole.EMPLOYEE),
                userService.countByRole(UserRole.CUSTOMER)
        ));
    }

    /**
     * Extrait le role de l'authentification.
     */
    private UserRole extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .map(UserRole::valueOf)
                .findFirst()
                .orElse(UserRole.CUSTOMER);
    }
}
