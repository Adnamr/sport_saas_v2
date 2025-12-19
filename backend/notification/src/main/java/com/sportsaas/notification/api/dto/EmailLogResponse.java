package com.sportsaas.notification.api.dto;

import com.sportsaas.notification.domain.EmailStatus;
import com.sportsaas.notification.domain.EmailType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de reponse pour un log d'email.
 */
public record EmailLogResponse(
    UUID id,
    EmailType emailType,
    String recipientEmail,
    String recipientName,
    String subject,
    String templateName,
    EmailStatus status,
    String referenceType,
    UUID referenceId,
    int attemptCount,
    LocalDateTime sentAt,
    String errorMessage,
    LocalDateTime createdAt
) {}
