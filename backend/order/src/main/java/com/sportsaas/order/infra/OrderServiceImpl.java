package com.sportsaas.order.infra;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.StockService;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service de gestion des commandes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public Order create(Order order) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        order.setTenantId(tenantId);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.DRAFT);

        Order saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getOrderNumber());
        return saved;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Page<Order> findByCustomerId(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    @Transactional
    public Order addItem(UUID orderId, UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("La quantite doit etre positive");
        }

        Order order = findByIdOrThrow(orderId);
        validateOrderModifiable(order);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        OrderItem item = new OrderItem();
        item.setTenantId(order.getTenantId());
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductSku(product.getSku());
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.recalculateLineTotal();

        order.addItem(item);
        Order saved = orderRepository.save(order);

        log.info("Item added to order {}: {} x {}", order.getOrderNumber(), quantity, product.getName());
        return saved;
    }

    @Override
    @Transactional
    public Order updateItemQuantity(UUID orderId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("La quantite doit etre positive");
        }

        Order order = findByIdOrThrow(orderId);
        validateOrderModifiable(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("OrderItem", itemId));

        item.setQuantity(quantity);
        order.recalculateTotals();

        Order saved = orderRepository.save(order);
        log.info("Item quantity updated in order {}: {}", order.getOrderNumber(), quantity);
        return saved;
    }

    @Override
    @Transactional
    public Order removeItem(UUID orderId, UUID itemId) {
        Order order = findByIdOrThrow(orderId);
        validateOrderModifiable(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("OrderItem", itemId));

        order.removeItem(item);
        Order saved = orderRepository.save(order);

        log.info("Item removed from order {}", order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public Order submit(UUID id) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestException("Seules les commandes en brouillon peuvent etre soumises");
        }

        if (order.getItems().isEmpty()) {
            throw new BadRequestException("La commande doit contenir au moins un article");
        }

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        Order saved = orderRepository.save(order);

        log.info("Order submitted: {}", order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public Order markAsPaid(UUID id, String paymentReference) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("La commande n'est pas en attente de paiement");
        }

        // Reserve stock for all items
        for (OrderItem item : order.getItems()) {
            stockService.removeStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getOrderNumber(),
                    "Commande payee"
            );
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        log.info("Order marked as paid: {} (ref: {})", order.getOrderNumber(), paymentReference);
        return saved;
    }

    @Override
    @Transactional
    public Order startProcessing(UUID id) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException("La commande doit etre payee pour etre traitee");
        }

        order.setStatus(OrderStatus.PROCESSING);
        Order saved = orderRepository.save(order);

        log.info("Order processing started: {}", order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public Order markAsReady(UUID id) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("La commande doit etre en preparation");
        }

        order.setStatus(OrderStatus.READY);
        Order saved = orderRepository.save(order);

        log.info("Order ready: {}", order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public Order ship(UUID id, String trackingNumber) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.READY) {
            throw new BadRequestException("La commande doit etre prete pour etre expediee");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") +
                "Tracking: " + trackingNumber);
        Order saved = orderRepository.save(order);

        log.info("Order shipped: {} (tracking: {})", order.getOrderNumber(), trackingNumber);
        return saved;
    }

    @Override
    @Transactional
    public Order deliver(UUID id) {
        Order order = findByIdOrThrow(id);

        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.READY) {
            throw new BadRequestException("La commande doit etre expediee ou prete pour etre livree");
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        log.info("Order delivered: {}", order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public Order cancel(UUID id, String reason) {
        Order order = findByIdOrThrow(id);

        if (!order.canCancel()) {
            throw new BadRequestException("Cette commande ne peut pas etre annulee");
        }

        // Restore stock if order was paid
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PROCESSING) {
            for (OrderItem item : order.getItems()) {
                stockService.addStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        order.getOrderNumber(),
                        "Commande annulee: " + reason
                );
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        Order saved = orderRepository.save(order);

        log.info("Order cancelled: {} (reason: {})", order.getOrderNumber(), reason);
        return saved;
    }

    @Override
    @Transactional
    public Order applyDiscount(UUID id, String discountCode, BigDecimal amount) {
        Order order = findByIdOrThrow(id);
        validateOrderModifiable(order);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant de la remise ne peut pas etre negatif");
        }

        order.setDiscountCode(discountCode);
        order.setDiscountAmount(amount);
        order.recalculateTotals();

        Order saved = orderRepository.save(order);
        log.info("Discount applied to order {}: {} ({})", order.getOrderNumber(), amount, discountCode);
        return saved;
    }

    private Order findByIdOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    private void validateOrderModifiable(Order order) {
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BadRequestException("Seules les commandes en brouillon peuvent etre modifiees");
        }
    }

    private String generateOrderNumber() {
        String prefix = "ORD";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + timestamp + "-" + random;
    }
}
