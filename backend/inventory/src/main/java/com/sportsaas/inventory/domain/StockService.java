package com.sportsaas.inventory.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion du stock.
 */
public interface StockService {

    /**
     * Recupere tous les stocks avec pagination.
     */
    Page<StockItem> findAll(Pageable pageable);

    /**
     * Recupere le stock d'un produit.
     */
    Optional<StockItem> findByProductId(UUID productId);

    /**
     * Recupere ou cree le stock pour un produit.
     */
    StockItem getOrCreateForProduct(UUID productId);

    /**
     * Ajoute du stock pour un produit.
     */
    StockItem addStock(UUID productId, int quantity, String reference, String reason);

    /**
     * Retire du stock pour un produit.
     */
    StockItem removeStock(UUID productId, int quantity, String reference, String reason);

    /**
     * Ajuste le stock a une quantite donnee.
     */
    StockItem adjustStock(UUID productId, int newQuantity, String reason);

    /**
     * Recupere les produits en stock bas.
     */
    List<StockItem> findLowStock();

    /**
     * Recupere les produits en rupture.
     */
    List<StockItem> findOutOfStock();

    /**
     * Met a jour le seuil d'alerte.
     */
    StockItem updateLowStockThreshold(UUID productId, int threshold);

    /**
     * Met a jour l'emplacement.
     */
    StockItem updateLocation(UUID productId, String location);

    /**
     * Recupere l'historique des mouvements d'un produit.
     */
    List<StockMovement> getMovementHistory(UUID productId);
}
