package com.sportsaas.catalog.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les Products.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Page<Product> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Optional<Product> findById(UUID id);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Optional<Product> findBySku(String sku);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.category.id = :categoryId")
    Page<Product> findByStatusAndCategoryId(@Param("status") ProductStatus status,
                                            @Param("categoryId") UUID categoryId,
                                            Pageable pageable);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> search(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "attributes", "category"})
    List<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status);
}
