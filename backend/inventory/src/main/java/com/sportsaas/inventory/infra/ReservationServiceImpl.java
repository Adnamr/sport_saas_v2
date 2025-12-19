package com.sportsaas.inventory.infra;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.*;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service de gestion des reservations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Reservation create(UUID productId, int quantity, Duration duration, String reference, UUID customerId) {
        if (quantity <= 0) {
            throw new BadRequestException("La quantite doit etre positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseThrow(() -> new BadRequestException("Aucun stock configure pour ce produit"));

        if (quantity > stockItem.getAvailableQuantity()) {
            throw new BadRequestException("Quantite insuffisante disponible pour reservation");
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setTenantId(TenantContext.requireCurrentTenant());
        reservation.setProduct(product);
        reservation.setQuantity(quantity);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setExpiresAt(LocalDateTime.now().plus(duration));
        reservation.setReference(reference);
        reservation.setCustomerId(customerId);

        Reservation saved = reservationRepository.save(reservation);

        // Reserve stock
        int quantityBefore = stockItem.getPhysicalQuantity();
        stockItem.reserve(quantity);
        stockItemRepository.save(stockItem);

        // Record movement
        recordMovement(product, MovementType.RESERVATION, quantity,
                quantityBefore, stockItem.getPhysicalQuantity(),
                reference, saved.getId(), "Reservation created");

        log.info("Reservation created: {} units of product {} for customer {}",
                quantity, productId, customerId);
        return saved;
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Page<Reservation> findByProductId(UUID productId, Pageable pageable) {
        return reservationRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Override
    public List<Reservation> findByCustomerId(UUID customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Reservation> findActiveByCustomerId(UUID customerId) {
        return reservationRepository.findByCustomerIdAndStatus(customerId, ReservationStatus.ACTIVE);
    }

    @Override
    public Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable) {
        return reservationRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    @Transactional
    public Reservation confirm(UUID id) {
        Reservation reservation = findByIdOrThrow(id);

        if (!reservation.canConfirm()) {
            throw new BadRequestException("Cette reservation ne peut pas etre confirmee");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation {} confirmed", id);
        return saved;
    }

    @Override
    @Transactional
    public Reservation cancel(UUID id) {
        Reservation reservation = findByIdOrThrow(id);

        if (!reservation.canCancel()) {
            throw new BadRequestException("Cette reservation ne peut pas etre annulee");
        }

        // Release stock
        releaseStock(reservation, "Reservation cancelled");

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation {} cancelled", id);
        return saved;
    }

    @Override
    @Transactional
    public Reservation extend(UUID id, Duration additionalDuration) {
        Reservation reservation = findByIdOrThrow(id);

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BadRequestException("Seules les reservations actives peuvent etre prolongees");
        }

        reservation.setExpiresAt(reservation.getExpiresAt().plus(additionalDuration));
        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation {} extended until {}", id, saved.getExpiresAt());
        return saved;
    }

    @Override
    @Transactional
    public int processExpiredReservations() {
        List<Reservation> expired = reservationRepository.findExpiredReservations(LocalDateTime.now());
        int count = 0;

        for (Reservation reservation : expired) {
            try {
                releaseStock(reservation, "Reservation expired");
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
                count++;
                log.info("Expired reservation {} processed", reservation.getId());
            } catch (Exception e) {
                log.error("Error processing expired reservation {}", reservation.getId(), e);
            }
        }

        log.info("Processed {} expired reservations", count);
        return count;
    }

    @Override
    @Transactional
    public Reservation release(UUID id) {
        Reservation reservation = findByIdOrThrow(id);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Seules les reservations confirmees peuvent etre liberees");
        }

        // For confirmed reservations, we just mark as released (stock was already removed via OUT movement)
        reservation.setStatus(ReservationStatus.RELEASED);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation {} released", id);
        return saved;
    }

    private Reservation findByIdOrThrow(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation", id));
    }

    private void releaseStock(Reservation reservation, String reason) {
        StockItem stockItem = stockItemRepository.findByProductId(reservation.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("Stock for product", reservation.getProduct().getId()));

        int quantityBefore = stockItem.getPhysicalQuantity();
        stockItem.release(reservation.getQuantity());
        stockItemRepository.save(stockItem);

        recordMovement(reservation.getProduct(), MovementType.RELEASE, reservation.getQuantity(),
                quantityBefore, stockItem.getPhysicalQuantity(),
                reservation.getReference(), reservation.getId(), reason);
    }

    private void recordMovement(Product product, MovementType type, int quantity,
                                int quantityBefore, int quantityAfter,
                                String reference, UUID reservationId, String reason) {
        StockMovement movement = new StockMovement();
        movement.setTenantId(TenantContext.requireCurrentTenant());
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setReference(reference);
        movement.setReservationId(reservationId);
        movement.setReason(reason);

        stockMovementRepository.save(movement);
    }
}
