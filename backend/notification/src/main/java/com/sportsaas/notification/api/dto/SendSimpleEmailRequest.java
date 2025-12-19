package com.sportsaas.notification.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour l'envoi d'un email simple.
 */
public record SendSimpleEmailRequest(
    @NotBlank(message = "L'adresse email du destinataire est obligatoire")
    @Email(message = "L'adresse email doit etre valide")
    String recipientEmail,

    String recipientName,

    @NotBlank(message = "Le sujet est obligatoire")
    String subject,

    @NotBlank(message = "Le contenu est obligatoire")
    String content
) {}
