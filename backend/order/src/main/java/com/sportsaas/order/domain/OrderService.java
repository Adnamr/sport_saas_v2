package com.sportsaas.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des commandes.
 */
public interface OrderService {

    /**
     * Cree une nouvelle commande.
     */
    Order create(Order order);

    /**
     * Recupere une commande par ID.
     */
    Optional<Order> findById(UUID id);

    /**
     * Recupere une commande par numero.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Liste les commandes d'un client.
     */
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Liste les commandes par statut.
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Ajoute un item a la commande.
     */
    Order addItem(UUID orderId, UUID productId, int quantity);

    /**
     * Met a jour la quantite d'un item.
     */
    Order updateItemQuantity(UUID orderId, UUID itemId, int quantity);

    /**
     * Supprime un item de la commande.
     */
    Order removeItem(UUID orderId, UUID itemId);

    /**
     * Soumet la commande pour paiement.
     */
    Order submit(UUID id);

    /**
     * Marque la commande comme payee.
     */
    Order markAsPaid(UUID id, String paymentReference);

    /**
     * Passe la commande en preparation.
     */
    Order startProcessing(UUID id);

    /**
     * Marque la commande comme prete.
     */
    Order markAsReady(UUID id);

    /**
     * Marque la commande comme expediee.
     */
    Order ship(UUID id, String trackingNumber);

    /**
     * Marque la commande comme livree.
     */
    Order deliver(UUID id);

    /**
     * Annule la commande.
     */
    Order cancel(UUID id, String reason);

    /**
     * Applique un code de remise.
     */
    Order applyDiscount(UUID id, String discountCode, java.math.BigDecimal amount);
}
