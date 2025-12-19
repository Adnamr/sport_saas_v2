package com.sportsaas.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les StockItems.
 */
@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    Optional<StockItem> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);

    @Query("SELECT s FROM StockItem s WHERE s.availableQuantity <= s.lowStockThreshold")
    List<StockItem> findLowStock();

    @Query("SELECT s FROM StockItem s WHERE s.availableQuantity <= 0")
    List<StockItem> findOutOfStock();

    @Query("SELECT s FROM StockItem s WHERE s.location = :location")
    List<StockItem> findByLocation(@Param("location") String location);

    @Query("SELECT s FROM StockItem s JOIN FETCH s.product WHERE s.availableQuantity <= s.lowStockThreshold")
    List<StockItem> findLowStockWithProduct();
}
