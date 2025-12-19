package com.sportsaas.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour les EmailLogs.
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    Page<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail, Pageable pageable);

    Page<EmailLog> findByEmailTypeOrderByCreatedAtDesc(EmailType emailType, Pageable pageable);

    Page<EmailLog> findByStatusOrderByCreatedAtDesc(EmailStatus status, Pageable pageable);

    List<EmailLog> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);

    @Query("SELECT e FROM EmailLog e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<EmailLog> findPendingEmails();

    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.attemptCount < :maxAttempts ORDER BY e.createdAt ASC")
    List<EmailLog> findRetryableEmails(@Param("maxAttempts") int maxAttempts);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = 'SENT' AND e.sentAt >= :since")
    long countSentSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = 'FAILED' AND e.createdAt >= :since")
    long countFailedSince(@Param("since") LocalDateTime since);
}
