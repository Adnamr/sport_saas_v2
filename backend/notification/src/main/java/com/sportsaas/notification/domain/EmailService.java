package com.sportsaas.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des emails.
 */
public interface EmailService {

    /**
     * Envoie un email de bienvenue.
     */
    EmailLog sendWelcomeEmail(String recipientEmail, String recipientName, String tenantName);

    /**
     * Envoie un email de reinitialisation de mot de passe.
     */
    EmailLog sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink);

    /**
     * Envoie une confirmation de commande.
     */
    EmailLog sendOrderConfirmation(String recipientEmail, String recipientName,
                                    UUID orderId, String orderNumber, Map<String, Object> orderDetails);

    /**
     * Envoie une confirmation de location.
     */
    EmailLog sendRentalConfirmation(String recipientEmail, String recipientName,
                                     UUID rentalId, String rentalNumber, Map<String, Object> rentalDetails);

    /**
     * Envoie un rappel de retour de location.
     */
    EmailLog sendRentalReturnReminder(String recipientEmail, String recipientName,
                                       UUID rentalId, String rentalNumber, Map<String, Object> rentalDetails);

    /**
     * Envoie une facture.
     */
    EmailLog sendInvoiceEmail(String recipientEmail, String recipientName,
                               UUID invoiceId, String invoiceNumber, Map<String, Object> invoiceDetails);

    /**
     * Envoie une confirmation de paiement.
     */
    EmailLog sendPaymentConfirmation(String recipientEmail, String recipientName,
                                      UUID paymentId, String paymentReference, Map<String, Object> paymentDetails);

    /**
     * Envoie un rappel de paiement.
     */
    EmailLog sendPaymentReminder(String recipientEmail, String recipientName,
                                  UUID invoiceId, String invoiceNumber, Map<String, Object> invoiceDetails);

    /**
     * Envoie un email generique avec template.
     */
    EmailLog sendTemplatedEmail(String recipientEmail, String recipientName, String subject,
                                 String templateName, Map<String, Object> variables);

    /**
     * Envoie un email generique en texte brut.
     */
    EmailLog sendSimpleEmail(String recipientEmail, String recipientName, String subject, String content);

    /**
     * Recupere un log par ID.
     */
    Optional<EmailLog> findById(UUID id);

    /**
     * Recupere les logs par destinataire.
     */
    Page<EmailLog> findByRecipient(String email, Pageable pageable);

    /**
     * Recupere les logs par type.
     */
    Page<EmailLog> findByType(EmailType type, Pageable pageable);

    /**
     * Recupere les logs par statut.
     */
    Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);

    /**
     * Recupere les logs par reference.
     */
    List<EmailLog> findByReference(String referenceType, UUID referenceId);

    /**
     * Reessaie l'envoi d'un email echoue.
     */
    EmailLog retry(UUID emailLogId);

    /**
     * Traite les emails en attente.
     */
    int processPendingEmails();

    /**
     * Reessaie les emails echoues.
     */
    int retryFailedEmails(int maxAttempts);

    /**
     * Statistiques d'envoi.
     */
    record EmailStats(long sentLast24h, long failedLast24h, long pending) {}

    /**
     * Recupere les statistiques.
     */
    EmailStats getStats();
}
