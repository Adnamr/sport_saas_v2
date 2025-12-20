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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service de gestion du stock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<StockItem> findAll(Pageable pageable) {
        return stockItemRepository.findAll(pageable);
    }

    @Override
    public Optional<StockItem> findByProductId(UUID productId) {
        return stockItemRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public StockItem getOrCreateForProduct(UUID productId) {
        return stockItemRepository.findByProductId(productId)
                .orElseGet(() -> createStockItem(productId));
    }

    @Override
    @Transactional
    public StockItem addStock(UUID productId, int quantity, String reference, String reason) {
        if (quantity <= 0) {
            throw new BadRequestException("La quantite doit etre positive");
        }

        StockItem stockItem = getOrCreateForProduct(productId);
        int quantityBefore = stockItem.getPhysicalQuantity();

        stockItem.addStock(quantity);
        StockItem saved = stockItemRepository.save(stockItem);

        recordMovement(stockItem.getProduct(), MovementType.IN, quantity,
                quantityBefore, saved.getPhysicalQuantity(), reference, null, reason);

        log.info("Stock added: {} units of product {} (new total: {})",
                quantity, productId, saved.getPhysicalQuantity());
        return saved;
    }

    @Override
    @Transactional
    public StockItem removeStock(UUID productId, int quantity, String reference, String reason) {
        if (quantity <= 0) {
            throw new BadRequestException("La quantite doit etre positive");
        }

        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock for product", productId));

        if (quantity > stockItem.getAvailableQuantity()) {
            throw new BadRequestException("Quantite insuffisante disponible");
        }

        int quantityBefore = stockItem.getPhysicalQuantity();
        stockItem.removeStock(quantity);
        StockItem saved = stockItemRepository.save(stockItem);

        recordMovement(stockItem.getProduct(), MovementType.OUT, quantity,
                quantityBefore, saved.getPhysicalQuantity(), reference, null, reason);

        log.info("Stock removed: {} units of product {} (new total: {})",
                quantity, productId, saved.getPhysicalQuantity());
        return saved;
    }

    @Override
    @Transactional
    public StockItem adjustStock(UUID productId, int newQuantity, String reason) {
        if (newQuantity < 0) {
            throw new BadRequestException("La quantite ne peut pas etre negative");
        }

        StockItem stockItem = getOrCreateForProduct(productId);
        int quantityBefore = stockItem.getPhysicalQuantity();
        int difference = newQuantity - quantityBefore;

        stockItem.setPhysicalQuantity(newQuantity);
        stockItem.recalculateAvailable();
        StockItem saved = stockItemRepository.save(stockItem);

        recordMovement(stockItem.getProduct(), MovementType.ADJUSTMENT, Math.abs(difference),
                quantityBefore, newQuantity, null, null, reason);

        log.info("Stock adjusted for product {}: {} -> {} ({})",
                productId, quantityBefore, newQuantity, reason);
        return saved;
    }

    @Override
    public List<StockItem> findLowStock() {
        return stockItemRepository.findLowStock();
    }

    @Override
    public List<StockItem> findOutOfStock() {
        return stockItemRepository.findOutOfStock();
    }

    @Override
    @Transactional
    public StockItem updateLowStockThreshold(UUID productId, int threshold) {
        if (threshold < 0) {
            throw new BadRequestException("Le seuil ne peut pas etre negatif");
        }

        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock for product", productId));

        stockItem.setLowStockThreshold(threshold);
        StockItem saved = stockItemRepository.save(stockItem);

        log.info("Low stock threshold updated for product {}: {}", productId, threshold);
        return saved;
    }

    @Override
    @Transactional
    public StockItem updateLocation(UUID productId, String location) {
        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock for product", productId));

        stockItem.setLocation(location);
        StockItem saved = stockItemRepository.save(stockItem);

        log.info("Location updated for product {}: {}", productId, location);
        return saved;
    }

    @Override
    public List<StockMovement> getMovementHistory(UUID productId) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    private StockItem createStockItem(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        StockItem stockItem = new StockItem();
        stockItem.setTenantId(TenantContext.requireCurrentTenant());
        stockItem.setProduct(product);
        stockItem.setPhysicalQuantity(0);
        stockItem.setReservedQuantity(0);
        stockItem.setAvailableQuantity(0);

        return stockItemRepository.save(stockItem);
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
        movement.setPerformedBy(getCurrentUserId());

        stockMovementRepository.save(movement);
    }

    private UUID getCurrentUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                return UUID.fromString(auth.getName());
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID", e);
        }
        return null;
    }
}
