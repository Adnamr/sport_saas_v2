package com.sportsaas.order.infra;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.StockItem;
import com.sportsaas.inventory.domain.StockItemRepository;
import com.sportsaas.order.domain.*;
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
 * Implementation du service de gestion des locations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;

    @Override
    @Transactional
    public Rental create(Rental rental) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        rental.setTenantId(tenantId);
        rental.setRentalNumber(generateRentalNumber());
        rental.setStatus(RentalStatus.PENDING);

        // Validate product
        Product product = productRepository.findById(rental.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("Product", rental.getProduct().getId()));

        rental.setProductName(product.getName());

        // Check availability
        if (!isProductAvailable(product.getId(), rental.getStartDate(), rental.getEndDate(), rental.getQuantity())) {
            throw new BadRequestException("Produit non disponible pour cette periode");
        }

        rental.recalculateTotals();

        Rental saved = rentalRepository.save(rental);
        log.info("Rental created: {} for product {}", saved.getRentalNumber(), product.getName());
        return saved;
    }

    @Override
    public Optional<Rental> findById(UUID id) {
        return rentalRepository.findById(id);
    }

    @Override
    public Optional<Rental> findByRentalNumber(String rentalNumber) {
        return rentalRepository.findByRentalNumber(rentalNumber);
    }

    @Override
    public Page<Rental> findByCustomerId(UUID customerId, Pageable pageable) {
        return rentalRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public Page<Rental> findByStatus(RentalStatus status, Pageable pageable) {
        return rentalRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<Rental> findByProductId(UUID productId, Pageable pageable) {
        return rentalRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Override
    @Transactional
    public Rental confirm(UUID id) {
        Rental rental = findByIdOrThrow(id);

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new BadRequestException("Seules les locations en attente peuvent etre confirmees");
        }

        // Re-check availability
        if (!isProductAvailable(rental.getProduct().getId(), rental.getStartDate(), rental.getEndDate(), rental.getQuantity())) {
            throw new BadRequestException("Produit non disponible pour cette periode");
        }

        rental.setStatus(RentalStatus.CONFIRMED);
        Rental saved = rentalRepository.save(rental);

        log.info("Rental confirmed: {}", rental.getRentalNumber());
        return saved;
    }

    @Override
    @Transactional
    public Rental start(UUID id, String conditionAtStart) {
        Rental rental = findByIdOrThrow(id);

        if (!rental.canStart()) {
            throw new BadRequestException("Cette location ne peut pas demarrer");
        }

        // Reserve stock
        StockItem stockItem = stockItemRepository.findByProductId(rental.getProduct().getId())
                .orElseThrow(() -> new BadRequestException("Stock non configure pour ce produit"));

        if (rental.getQuantity() > stockItem.getAvailableQuantity()) {
            throw new BadRequestException("Stock insuffisant");
        }

        stockItem.reserve(rental.getQuantity());
        stockItemRepository.save(stockItem);

        rental.setStatus(RentalStatus.ACTIVE);
        rental.setConditionAtStart(conditionAtStart);
        Rental saved = rentalRepository.save(rental);

        log.info("Rental started: {}", rental.getRentalNumber());
        return saved;
    }

    @Override
    @Transactional
    public Rental returnRental(UUID id, String conditionAtReturn) {
        Rental rental = findByIdOrThrow(id);

        if (!rental.canReturn()) {
            throw new BadRequestException("Cette location ne peut pas etre retournee");
        }

        // Release stock
        StockItem stockItem = stockItemRepository.findByProductId(rental.getProduct().getId())
                .orElseThrow(() -> new BadRequestException("Stock non configure pour ce produit"));

        stockItem.release(rental.getQuantity());
        stockItemRepository.save(stockItem);

        rental.setStatus(RentalStatus.RETURNED);
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setConditionAtReturn(conditionAtReturn);
        rental.recalculateTotals();

        Rental saved = rentalRepository.save(rental);
        log.info("Rental returned: {}", rental.getRentalNumber());
        return saved;
    }

    @Override
    @Transactional
    public Rental cancel(UUID id, String reason) {
        Rental rental = findByIdOrThrow(id);

        if (!rental.canCancel()) {
            throw new BadRequestException("Cette location ne peut pas etre annulee");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setNotes((rental.getNotes() != null ? rental.getNotes() + "\n" : "") +
                "Annulee: " + reason);
        Rental saved = rentalRepository.save(rental);

        log.info("Rental cancelled: {} (reason: {})", rental.getRentalNumber(), reason);
        return saved;
    }

    @Override
    @Transactional
    public Rental extend(UUID id, LocalDateTime newEndDate) {
        Rental rental = findByIdOrThrow(id);

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BadRequestException("Seules les locations actives peuvent etre prolongees");
        }

        if (newEndDate.isBefore(rental.getEndDate())) {
            throw new BadRequestException("La nouvelle date doit etre apres la date actuelle");
        }

        rental.setEndDate(newEndDate);
        rental.recalculateTotals();

        Rental saved = rentalRepository.save(rental);
        log.info("Rental extended: {} until {}", rental.getRentalNumber(), newEndDate);
        return saved;
    }

    @Override
    @Transactional
    public Rental addExtraCharges(UUID id, BigDecimal amount, String reason) {
        Rental rental = findByIdOrThrow(id);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant ne peut pas etre negatif");
        }

        rental.setExtraCharges(rental.getExtraCharges().add(amount));
        rental.setExtraChargesNotes((rental.getExtraChargesNotes() != null ?
                rental.getExtraChargesNotes() + "\n" : "") + amount + " - " + reason);
        rental.recalculateTotals();

        Rental saved = rentalRepository.save(rental);
        log.info("Extra charges added to rental {}: {} ({})", rental.getRentalNumber(), amount, reason);
        return saved;
    }

    @Override
    @Transactional
    public int processOverdueRentals() {
        List<Rental> overdue = rentalRepository.findOverdueRentals(LocalDateTime.now());
        int count = 0;

        for (Rental rental : overdue) {
            try {
                rental.setStatus(RentalStatus.OVERDUE);
                rentalRepository.save(rental);
                count++;
                log.info("Rental marked as overdue: {}", rental.getRentalNumber());
            } catch (Exception e) {
                log.error("Error processing overdue rental {}", rental.getRentalNumber(), e);
            }
        }

        log.info("Processed {} overdue rentals", count);
        return count;
    }

    @Override
    public List<Rental> findOverdue() {
        return rentalRepository.findOverdueRentals(LocalDateTime.now());
    }

    @Override
    public boolean isProductAvailable(UUID productId, LocalDateTime startDate, LocalDateTime endDate, int quantity) {
        // Check stock availability
        Optional<StockItem> stockOpt = stockItemRepository.findByProductId(productId);
        if (stockOpt.isEmpty()) {
            return false;
        }

        StockItem stockItem = stockOpt.get();

        // Get reserved quantity for overlapping rentals (checks date range overlap)
        Integer reservedQty = rentalRepository.sumOverlappingQuantityByProductId(productId, startDate, endDate);
        int reserved = reservedQty != null ? reservedQty : 0;

        int available = stockItem.getPhysicalQuantity() - reserved;
        return available >= quantity;
    }

    private Rental findByIdOrThrow(UUID id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental", id));
    }

    private String generateRentalNumber() {
        String prefix = "RNT";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + timestamp + "-" + random;
    }
}
