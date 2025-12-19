package com.sportsaas.notification.infra;

import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.notification.domain.*;
import com.sportsaas.tenant.domain.TenantContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation du service d'envoi d'emails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.email.from:noreply@sportsaas.com}")
    private String fromEmail;

    @Value("${app.email.from-name:Sport SaaS}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Override
    @Transactional
    public EmailLog sendWelcomeEmail(String recipientEmail, String recipientName, String tenantName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("tenantName", tenantName);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Bienvenue sur " + tenantName,
                "welcome",
                variables,
                EmailType.WELCOME,
                null,
                null
        );
    }

    @Override
    @Transactional
    public EmailLog sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("resetLink", resetLink);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Reinitialisation de votre mot de passe",
                "password-reset",
                variables,
                EmailType.PASSWORD_RESET,
                null,
                null
        );
    }

    @Override
    @Transactional
    public EmailLog sendOrderConfirmation(String recipientEmail, String recipientName,
                                           UUID orderId, String orderNumber, Map<String, Object> orderDetails) {
        Map<String, Object> variables = orderDetails != null ? new HashMap<>(orderDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("orderNumber", orderNumber);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Confirmation de votre commande " + orderNumber,
                "order-confirmation",
                variables,
                EmailType.ORDER_CONFIRMATION,
                "ORDER",
                orderId
        );
    }

    @Override
    @Transactional
    public EmailLog sendRentalConfirmation(String recipientEmail, String recipientName,
                                            UUID rentalId, String rentalNumber, Map<String, Object> rentalDetails) {
        Map<String, Object> variables = rentalDetails != null ? new HashMap<>(rentalDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("rentalNumber", rentalNumber);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Confirmation de votre location " + rentalNumber,
                "rental-confirmation",
                variables,
                EmailType.RENTAL_CONFIRMATION,
                "RENTAL",
                rentalId
        );
    }

    @Override
    @Transactional
    public EmailLog sendRentalReturnReminder(String recipientEmail, String recipientName,
                                              UUID rentalId, String rentalNumber, Map<String, Object> rentalDetails) {
        Map<String, Object> variables = rentalDetails != null ? new HashMap<>(rentalDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("rentalNumber", rentalNumber);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Rappel: Retour de votre location " + rentalNumber,
                "rental-return-reminder",
                variables,
                EmailType.RENTAL_RETURN_REMINDER,
                "RENTAL",
                rentalId
        );
    }

    @Override
    @Transactional
    public EmailLog sendInvoiceEmail(String recipientEmail, String recipientName,
                                      UUID invoiceId, String invoiceNumber, Map<String, Object> invoiceDetails) {
        Map<String, Object> variables = invoiceDetails != null ? new HashMap<>(invoiceDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("invoiceNumber", invoiceNumber);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Votre facture " + invoiceNumber,
                "invoice",
                variables,
                EmailType.INVOICE,
                "INVOICE",
                invoiceId
        );
    }

    @Override
    @Transactional
    public EmailLog sendPaymentConfirmation(String recipientEmail, String recipientName,
                                             UUID paymentId, String paymentReference, Map<String, Object> paymentDetails) {
        Map<String, Object> variables = paymentDetails != null ? new HashMap<>(paymentDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("paymentReference", paymentReference);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Confirmation de paiement " + paymentReference,
                "payment-confirmation",
                variables,
                EmailType.PAYMENT_CONFIRMATION,
                "PAYMENT",
                paymentId
        );
    }

    @Override
    @Transactional
    public EmailLog sendPaymentReminder(String recipientEmail, String recipientName,
                                         UUID invoiceId, String invoiceNumber, Map<String, Object> invoiceDetails) {
        Map<String, Object> variables = invoiceDetails != null ? new HashMap<>(invoiceDetails) : new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("invoiceNumber", invoiceNumber);

        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                "Rappel: Facture " + invoiceNumber + " en attente de paiement",
                "payment-reminder",
                variables,
                EmailType.PAYMENT_REMINDER,
                "INVOICE",
                invoiceId
        );
    }

    @Override
    @Transactional
    public EmailLog sendTemplatedEmail(String recipientEmail, String recipientName, String subject,
                                         String templateName, Map<String, Object> variables) {
        return sendTemplatedEmail(
                recipientEmail,
                recipientName,
                subject,
                templateName,
                variables,
                EmailType.GENERIC,
                null,
                null
        );
    }

    @Override
    @Transactional
    public EmailLog sendSimpleEmail(String recipientEmail, String recipientName, String subject, String content) {
        EmailLog emailLog = createEmailLog(
                recipientEmail,
                recipientName,
                subject,
                null,
                EmailType.GENERIC,
                null,
                null
        );

        try {
            if (emailEnabled) {
                sendEmail(recipientEmail, recipientName, subject, content, false);
            }
            emailLog.markSent();
            log.info("Simple email sent to {}: {}", recipientEmail, subject);
        } catch (Exception e) {
            emailLog.markFailed(e.getMessage());
            log.error("Failed to send simple email to {}: {}", recipientEmail, e.getMessage());
        }

        return emailLogRepository.save(emailLog);
    }

    @Override
    public Optional<EmailLog> findById(UUID id) {
        return emailLogRepository.findById(id);
    }

    @Override
    public Page<EmailLog> findByRecipient(String email, Pageable pageable) {
        return emailLogRepository.findByRecipientEmailOrderByCreatedAtDesc(email, pageable);
    }

    @Override
    public Page<EmailLog> findByType(EmailType type, Pageable pageable) {
        return emailLogRepository.findByEmailTypeOrderByCreatedAtDesc(type, pageable);
    }

    @Override
    public Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable) {
        return emailLogRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public List<EmailLog> findByReference(String referenceType, UUID referenceId) {
        return emailLogRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    @Override
    @Transactional
    public EmailLog retry(UUID emailLogId) {
        EmailLog emailLog = emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new NotFoundException("EmailLog", emailLogId));

        if (emailLog.getStatus() != EmailStatus.FAILED) {
            throw new BadRequestException("Seuls les emails echoues peuvent etre retentes");
        }

        emailLog.incrementAttempt();

        try {
            String htmlContent = emailLog.getHtmlContent();
            if (htmlContent != null && emailEnabled) {
                sendEmail(emailLog.getRecipientEmail(), emailLog.getRecipientName(),
                        emailLog.getSubject(), htmlContent, true);
            }
            emailLog.markSent();
            log.info("Email retry successful for {}", emailLog.getRecipientEmail());
        } catch (Exception e) {
            emailLog.markFailed(e.getMessage());
            log.error("Email retry failed for {}: {}", emailLog.getRecipientEmail(), e.getMessage());
        }

        return emailLogRepository.save(emailLog);
    }

    @Override
    @Transactional
    public int processPendingEmails() {
        List<EmailLog> pending = emailLogRepository.findPendingEmails();
        int processed = 0;

        for (EmailLog emailLog : pending) {
            try {
                emailLog.incrementAttempt();
                if (emailLog.getHtmlContent() != null && emailEnabled) {
                    sendEmail(emailLog.getRecipientEmail(), emailLog.getRecipientName(),
                            emailLog.getSubject(), emailLog.getHtmlContent(), true);
                }
                emailLog.markSent();
                processed++;
            } catch (Exception e) {
                emailLog.markFailed(e.getMessage());
                log.error("Failed to process pending email {}: {}", emailLog.getId(), e.getMessage());
            }
            emailLogRepository.save(emailLog);
        }

        log.info("Processed {} pending emails", processed);
        return processed;
    }

    @Override
    @Transactional
    public int retryFailedEmails(int maxAttempts) {
        List<EmailLog> failed = emailLogRepository.findRetryableEmails(maxAttempts);
        int retried = 0;

        for (EmailLog emailLog : failed) {
            try {
                EmailLog retriedEmail = retry(emailLog.getId());
                if (retriedEmail.getStatus() == EmailStatus.SENT) {
                    retried++;
                }
            } catch (Exception e) {
                log.error("Failed to retry email {}: {}", emailLog.getId(), e.getMessage());
            }
        }

        log.info("Retried {} failed emails", retried);
        return retried;
    }

    @Override
    public EmailStats getStats() {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        long sent = emailLogRepository.countSentSince(last24h);
        long failed = emailLogRepository.countFailedSince(last24h);
        long pending = emailLogRepository.countPendingEmails();

        return new EmailStats(sent, failed, pending);
    }

    private EmailLog sendTemplatedEmail(String recipientEmail, String recipientName, String subject,
                                          String templateName, Map<String, Object> variables,
                                          EmailType emailType, String referenceType, UUID referenceId) {
        EmailLog emailLog = createEmailLog(
                recipientEmail,
                recipientName,
                subject,
                templateName,
                emailType,
                referenceType,
                referenceId
        );

        try {
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/" + templateName, context);
            emailLog.setHtmlContent(htmlContent);

            if (emailEnabled) {
                sendEmail(recipientEmail, recipientName, subject, htmlContent, true);
            }
            emailLog.markSent();
            log.info("Templated email sent to {}: {} (template: {})", recipientEmail, subject, templateName);
        } catch (Exception e) {
            emailLog.markFailed(e.getMessage());
            log.error("Failed to send templated email to {}: {}", recipientEmail, e.getMessage());
        }

        return emailLogRepository.save(emailLog);
    }

    private EmailLog createEmailLog(String recipientEmail, String recipientName, String subject,
                                     String templateName, EmailType emailType,
                                     String referenceType, UUID referenceId) {
        EmailLog emailLog = new EmailLog();

        UUID tenantId = TenantContext.getCurrentTenant();
        emailLog.setTenantId(tenantId);
        emailLog.setEmailType(emailType);
        emailLog.setRecipientEmail(recipientEmail);
        emailLog.setRecipientName(recipientName);
        emailLog.setSubject(subject);
        emailLog.setTemplateName(templateName);
        emailLog.setStatus(EmailStatus.PENDING);
        emailLog.setReferenceType(referenceType);
        emailLog.setReferenceId(referenceId);

        return emailLog;
    }

    private void sendEmail(String to, String toName, String subject, String content, boolean isHtml)
            throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        if (toName != null && !toName.isBlank()) {
            helper.setTo(new jakarta.mail.internet.InternetAddress(to, toName, "UTF-8"));
        } else {
            helper.setTo(to);
        }
        helper.setSubject(subject);
        helper.setText(content, isHtml);

        mailSender.send(message);
    }
}
