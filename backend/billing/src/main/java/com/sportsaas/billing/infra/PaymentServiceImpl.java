package com.sportsaas.billing.infra;

import com.sportsaas.billing.domain.*;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service de gestion des paiements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public Payment create(Payment payment) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        payment.setTenantId(tenantId);
        payment.setPaymentReference(generatePaymentReference());
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: {}", saved.getPaymentReference());
        return saved;
    }

    @Override
    @Transactional
    public Payment createForInvoice(UUID invoiceId, BigDecimal amount, PaymentMethod method) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice", invoiceId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit etre positif");
        }

        if (amount.compareTo(invoice.getBalanceDue()) > 0) {
            throw new BadRequestException("Le montant depasse le solde a payer");
        }

        Payment payment = new Payment();
        payment.setTenantId(invoice.getTenantId());
        payment.setPaymentReference(generatePaymentReference());
        payment.setInvoice(invoice);
        payment.setCustomerId(invoice.getCustomerId());
        payment.setCustomerName(invoice.getCustomerName());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setCurrency(invoice.getCurrency());
        payment.setDescription("Paiement facture " + invoice.getInvoiceNumber());

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created for invoice {}: {} {}", invoice.getInvoiceNumber(), amount, invoice.getCurrency());
        return saved;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByPaymentReference(String reference) {
        return paymentRepository.findByPaymentReference(reference);
    }

    @Override
    public Page<Payment> findByCustomerId(UUID customerId, Pageable pageable) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public Page<Payment> findByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public List<Payment> findByInvoiceId(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Override
    @Transactional
    public Payment process(UUID id) {
        Payment payment = findByIdOrThrow(id);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Seuls les paiements en attente peuvent etre traites");
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment processing: {}", payment.getPaymentReference());
        return saved;
    }

    @Override
    @Transactional
    public Payment complete(UUID id, String externalReference) {
        Payment payment = findByIdOrThrow(id);

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new BadRequestException("Ce paiement ne peut pas etre complete");
        }

        payment.complete(externalReference);

        // Update invoice if linked
        if (payment.getInvoice() != null) {
            Invoice invoice = payment.getInvoice();
            invoice.recordPayment(payment.getAmount());
            invoiceRepository.save(invoice);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Payment completed: {} (ref: {})", payment.getPaymentReference(), externalReference);
        return saved;
    }

    @Override
    @Transactional
    public Payment fail(UUID id, String reason) {
        Payment payment = findByIdOrThrow(id);

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new BadRequestException("Ce paiement ne peut pas etre marque comme echoue");
        }

        payment.fail(reason);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment failed: {} (reason: {})", payment.getPaymentReference(), reason);
        return saved;
    }

    @Override
    @Transactional
    public Payment cancel(UUID id) {
        Payment payment = findByIdOrThrow(id);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Seuls les paiements en attente peuvent etre annules");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment cancelled: {}", payment.getPaymentReference());
        return saved;
    }

    @Override
    @Transactional
    public Payment refund(UUID id, BigDecimal amount, String reason) {
        Payment payment = findByIdOrThrow(id);

        if (!payment.canRefund()) {
            throw new BadRequestException("Ce paiement ne peut pas etre rembourse");
        }

        BigDecimal maxRefund = payment.getAmount().subtract(payment.getRefundedAmount());
        if (amount.compareTo(maxRefund) > 0) {
            throw new BadRequestException("Le montant du remboursement depasse le maximum autorise");
        }

        payment.refund(amount);
        payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") +
                "Remboursement: " + amount + " - " + reason);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment refunded: {} - {} (reason: {})", payment.getPaymentReference(), amount, reason);
        return saved;
    }

    @Override
    public CustomerPaymentStats getCustomerStats(UUID customerId) {
        long count = paymentRepository.countByCustomerId(customerId);
        BigDecimal totalPaid = paymentRepository.sumCompletedAmountByCustomerId(customerId);

        return new CustomerPaymentStats(count, totalPaid);
    }

    private Payment findByIdOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id));
    }

    private String generatePaymentReference() {
        String prefix = "PAY";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + timestamp + "-" + random;
    }
}
