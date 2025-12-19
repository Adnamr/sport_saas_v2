package com.sportsaas.notification.api.dto;

import com.sportsaas.notification.domain.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * DTO pour l'envoi d'un email.
 */
public record SendEmailRequest(
    @NotBlank(message = "L'adresse email du destinataire est obligatoire")
    @Email(message = "L'adresse email doit etre valide")
    String recipientEmail,

    String recipientName,

    @NotBlank(message = "Le sujet est obligatoire")
    String subject,

    @NotNull(message = "Le type d'email est obligatoire")
    EmailType emailType,

    /** Nom du template (optionnel si emailType = GENERIC) */
    String templateName,

    /** Variables pour le template */
    Map<String, Object> variables,

    /** Type de reference (optionnel) */
    String referenceType,

    /** ID de reference (optionnel) */
    UUID referenceId
) {}
