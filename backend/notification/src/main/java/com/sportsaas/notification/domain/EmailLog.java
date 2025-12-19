package com.sportsaas.notification.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entite EmailLog - Journal des emails envoyes.
 */
@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_email_logs_recipient", columnList = "recipient_email"),
    @Index(name = "idx_email_logs_type", columnList = "email_type"),
    @Index(name = "idx_email_logs_status", columnList = "status"),
    @Index(name = "idx_email_logs_reference", columnList = "reference_type, reference_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false)
    private EmailType emailType;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(nullable = false)
    private String subject;

    @Column(name = "template_name")
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;

    /** Type de reference (ex: ORDER, RENTAL, INVOICE) */
    @Column(name = "reference_type")
    private String referenceType;

    /** ID de la reference */
    @Column(name = "reference_id")
    private UUID referenceId;

    /** Nombre de tentatives d'envoi */
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    /** Date d'envoi */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** Message d'erreur en cas d'echec */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Contenu HTML de l'email (pour debug) */
    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    /**
     * Marque l'email comme envoye.
     */
    public void markSent() {
        this.status = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marque l'email comme echoue.
     */
    public void markFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Incremente le compteur de tentatives.
     */
    public void incrementAttempt() {
        this.attemptCount++;
        this.status = EmailStatus.SENDING;
    }
}
