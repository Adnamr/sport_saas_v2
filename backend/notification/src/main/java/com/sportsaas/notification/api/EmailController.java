package com.sportsaas.notification.api;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.notification.api.dto.*;
import com.sportsaas.notification.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
@Tag(name = "Emails", description = "Gestion des notifications par email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final NotificationMapper notificationMapper;

    @PostMapping("/send")
    @Operation(summary = "Envoyer un email avec template")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmailLogResponse> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailLog emailLog = emailService.sendTemplatedEmail(
                request.recipientEmail(),
                request.recipientName(),
                request.subject(),
                request.templateName(),
                request.variables()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationMapper.toEmailLogResponse(emailLog));
    }

    @PostMapping("/send-simple")
    @Operation(summary = "Envoyer un email simple")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmailLogResponse> sendSimpleEmail(@Valid @RequestBody SendSimpleEmailRequest request) {
        EmailLog emailLog = emailService.sendSimpleEmail(
                request.recipientEmail(),
                request.recipientName(),
                request.subject(),
                request.content()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationMapper.toEmailLogResponse(emailLog));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un log d'email")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmailLogResponse> getById(@PathVariable UUID id) {
        EmailLog emailLog = emailService.findById(id)
                .orElseThrow(() -> new NotFoundException("EmailLog", id));
        return ResponseEntity.ok(notificationMapper.toEmailLogResponse(emailLog));
    }

    @GetMapping("/recipient/{email}")
    @Operation(summary = "Lister les emails par destinataire")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<EmailLogResponse>> getByRecipient(
            @PathVariable String email,
            Pageable pageable) {
        Page<EmailLog> page = emailService.findByRecipient(email, pageable);
        return ResponseEntity.ok(PageResponse.of(page, notificationMapper::toEmailLogResponse));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les emails par type")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<EmailLogResponse>> getByType(
            @PathVariable EmailType type,
            Pageable pageable) {
        Page<EmailLog> page = emailService.findByType(type, pageable);
        return ResponseEntity.ok(PageResponse.of(page, notificationMapper::toEmailLogResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les emails par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<EmailLogResponse>> getByStatus(
            @PathVariable EmailStatus status,
            Pageable pageable) {
        Page<EmailLog> page = emailService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, notificationMapper::toEmailLogResponse));
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    @Operation(summary = "Lister les emails par reference")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<EmailLogResponse>> getByReference(
            @PathVariable String referenceType,
            @PathVariable UUID referenceId) {
        List<EmailLog> logs = emailService.findByReference(referenceType, referenceId);
        return ResponseEntity.ok(notificationMapper.toEmailLogResponseList(logs));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Reessayer l'envoi d'un email echoue")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmailLogResponse> retry(@PathVariable UUID id) {
        EmailLog emailLog = emailService.retry(id);
        return ResponseEntity.ok(notificationMapper.toEmailLogResponse(emailLog));
    }

    @PostMapping("/process-pending")
    @Operation(summary = "Traiter les emails en attente")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Integer> processPending() {
        int count = emailService.processPendingEmails();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Reessayer les emails echoues")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Integer> retryFailed(@RequestParam(defaultValue = "3") int maxAttempts) {
        int count = emailService.retryFailedEmails(maxAttempts);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des emails")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmailStatsResponse> getStats() {
        return ResponseEntity.ok(notificationMapper.toEmailStatsResponse(emailService.getStats()));
    }
}
