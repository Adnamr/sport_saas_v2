package com.sportsaas.inventory.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Override
    @EntityGraph(attributePaths = {"product"})
    Page<StockItem> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"product"})
    Optional<StockItem> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT s FROM StockItem s WHERE s.availableQuantity <= s.lowStockThreshold")
    List<StockItem> findLowStock();

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT s FROM StockItem s WHERE s.availableQuantity <= 0")
    List<StockItem> findOutOfStock();

    @Query("SELECT s FROM StockItem s WHERE s.location = :location")
    List<StockItem> findByLocation(@Param("location") String location);

    @Query("SELECT s FROM StockItem s JOIN FETCH s.product WHERE s.availableQuantity <= s.lowStockThreshold")
    List<StockItem> findLowStockWithProduct();
}
