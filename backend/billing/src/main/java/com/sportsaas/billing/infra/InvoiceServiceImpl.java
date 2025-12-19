package com.sportsaas.billing.infra;

import com.sportsaas.billing.domain.*;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.Order;
import com.sportsaas.order.domain.OrderItem;
import com.sportsaas.order.domain.OrderRepository;
import com.sportsaas.order.domain.Rental;
import com.sportsaas.order.domain.RentalRepository;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service de gestion des factures.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final RentalRepository rentalRepository;

    @Override
    @Transactional
    public Invoice create(Invoice invoice) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {}", saved.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice createFromOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        Invoice invoice = new Invoice();
        invoice.setTenantId(order.getTenantId());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setOrder(order);
        invoice.setCustomerId(order.getCustomerId());
        invoice.setCustomerName(order.getCustomerName());
        invoice.setCustomerEmail(order.getCustomerEmail());
        invoice.setCustomerPhone(order.getCustomerPhone());
        invoice.setCustomerAddress(order.getBillingAddress());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setCurrency(order.getCurrency());

        // Add items from order
        for (OrderItem orderItem : order.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setTenantId(order.getTenantId());
            item.setDescription(orderItem.getProductName());
            item.setQuantity(orderItem.getQuantity());
            item.setUnitPrice(orderItem.getUnitPrice());
            item.setDiscountAmount(orderItem.getDiscountAmount());
            item.recalculateLineTotal();
            invoice.addItem(item);
        }

        invoice.setDiscountAmount(order.getDiscountAmount());
        invoice.setTaxAmount(order.getTaxAmount());
        invoice.recalculateTotals();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created from order {}: {}", order.getOrderNumber(), saved.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice createFromRental(UUID rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental", rentalId));

        Invoice invoice = new Invoice();
        invoice.setTenantId(rental.getTenantId());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setRental(rental);
        invoice.setCustomerId(rental.getCustomerId());
        invoice.setCustomerName(rental.getCustomerName());
        invoice.setCustomerEmail(rental.getCustomerEmail());
        invoice.setCustomerPhone(rental.getCustomerPhone());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setCurrency(rental.getCurrency());

        // Add rental as item
        InvoiceItem item = new InvoiceItem();
        item.setTenantId(rental.getTenantId());
        item.setDescription("Location: " + rental.getProductName() +
                " (" + rental.getRentalDays() + " jours)");
        item.setQuantity(rental.getQuantity());
        item.setUnitPrice(rental.getSubtotal().divide(BigDecimal.valueOf(rental.getQuantity()), 2, java.math.RoundingMode.HALF_UP));
        item.recalculateLineTotal();
        invoice.addItem(item);

        // Add extra charges if any
        if (rental.getExtraCharges() != null && rental.getExtraCharges().compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem extraItem = new InvoiceItem();
            extraItem.setTenantId(rental.getTenantId());
            extraItem.setDescription("Frais supplementaires: " +
                    (rental.getExtraChargesNotes() != null ? rental.getExtraChargesNotes() : ""));
            extraItem.setQuantity(1);
            extraItem.setUnitPrice(rental.getExtraCharges());
            extraItem.recalculateLineTotal();
            invoice.addItem(extraItem);
        }

        invoice.recalculateTotals();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created from rental {}: {}", rental.getRentalNumber(), saved.getInvoiceNumber());
        return saved;
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceRepository.findById(id);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    public Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    @Transactional
    public Invoice addItem(UUID invoiceId, String description, int quantity, BigDecimal unitPrice) {
        Invoice invoice = findByIdOrThrow(invoiceId);
        validateInvoiceModifiable(invoice);

        InvoiceItem item = new InvoiceItem();
        item.setTenantId(invoice.getTenantId());
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.recalculateLineTotal();

        invoice.addItem(item);
        Invoice saved = invoiceRepository.save(invoice);

        log.info("Item added to invoice {}: {}", invoice.getInvoiceNumber(), description);
        return saved;
    }

    @Override
    @Transactional
    public Invoice updateItem(UUID invoiceId, UUID itemId, int quantity, BigDecimal unitPrice) {
        Invoice invoice = findByIdOrThrow(invoiceId);
        validateInvoiceModifiable(invoice);

        InvoiceItem item = invoice.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("InvoiceItem", itemId));

        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        invoice.recalculateTotals();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Item updated in invoice {}", invoice.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice removeItem(UUID invoiceId, UUID itemId) {
        Invoice invoice = findByIdOrThrow(invoiceId);
        validateInvoiceModifiable(invoice);

        InvoiceItem item = invoice.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("InvoiceItem", itemId));

        invoice.removeItem(item);
        Invoice saved = invoiceRepository.save(invoice);

        log.info("Item removed from invoice {}", invoice.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice finalize(UUID id) {
        Invoice invoice = findByIdOrThrow(id);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BadRequestException("Seules les factures en brouillon peuvent etre finalisees");
        }

        if (invoice.getItems().isEmpty()) {
            throw new BadRequestException("La facture doit contenir au moins un article");
        }

        invoice.setStatus(InvoiceStatus.FINALIZED);
        Invoice saved = invoiceRepository.save(invoice);

        log.info("Invoice finalized: {}", invoice.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice send(UUID id) {
        Invoice invoice = findByIdOrThrow(id);

        if (invoice.getStatus() != InvoiceStatus.FINALIZED) {
            throw new BadRequestException("La facture doit etre finalisee avant d'etre envoyee");
        }

        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setSentAt(LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);

        log.info("Invoice sent: {}", invoice.getInvoiceNumber());
        return saved;
    }

    @Override
    @Transactional
    public Invoice applyDiscount(UUID id, BigDecimal amount) {
        Invoice invoice = findByIdOrThrow(id);
        validateInvoiceModifiable(invoice);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant de la remise ne peut pas etre negatif");
        }

        invoice.setDiscountAmount(amount);
        invoice.recalculateTotals();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Discount applied to invoice {}: {}", invoice.getInvoiceNumber(), amount);
        return saved;
    }

    @Override
    @Transactional
    public Invoice setTaxRate(UUID id, BigDecimal rate) {
        Invoice invoice = findByIdOrThrow(id);
        validateInvoiceModifiable(invoice);

        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Le taux de TVA doit etre entre 0 et 100");
        }

        invoice.setTaxRate(rate);
        invoice.recalculateTotals();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Tax rate set on invoice {}: {}%", invoice.getInvoiceNumber(), rate);
        return saved;
    }

    @Override
    @Transactional
    public Invoice cancel(UUID id, String reason) {
        Invoice invoice = findByIdOrThrow(id);

        if (!invoice.canCancel()) {
            throw new BadRequestException("Cette facture ne peut pas etre annulee");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancellationReason(reason);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice cancelled: {} (reason: {})", invoice.getInvoiceNumber(), reason);
        return saved;
    }

    @Override
    public List<Invoice> findOverdue() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    @Override
    @Transactional
    public int processOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(LocalDate.now());
        int count = 0;

        for (Invoice invoice : overdue) {
            try {
                if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
                    invoice.setStatus(InvoiceStatus.OVERDUE);
                    invoiceRepository.save(invoice);
                    count++;
                    log.info("Invoice marked as overdue: {}", invoice.getInvoiceNumber());
                }
            } catch (Exception e) {
                log.error("Error processing overdue invoice {}", invoice.getInvoiceNumber(), e);
            }
        }

        log.info("Processed {} overdue invoices", count);
        return count;
    }

    @Override
    public CustomerBillingStats getCustomerStats(UUID customerId) {
        long count = invoiceRepository.countByCustomerId(customerId);
        BigDecimal totalPaid = invoiceRepository.sumPaidTotalByCustomerId(customerId);
        BigDecimal balanceDue = invoiceRepository.sumBalanceDueByCustomerId(customerId);

        return new CustomerBillingStats(count, totalPaid, balanceDue);
    }

    private Invoice findByIdOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice", id));
    }

    private void validateInvoiceModifiable(Invoice invoice) {
        if (!invoice.canModify()) {
            throw new BadRequestException("Cette facture ne peut plus etre modifiee");
        }
    }

    private String generateInvoiceNumber() {
        String prefix = "INV";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + timestamp + "-" + random;
    }
}
