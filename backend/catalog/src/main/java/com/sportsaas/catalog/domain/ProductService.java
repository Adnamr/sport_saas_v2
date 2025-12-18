package com.sportsaas.catalog.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Product.
 */
public interface ProductService {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategory(UUID categoryId, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> search(String query, Pageable pageable);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    Product create(Product product);

    Product update(UUID id, Product product);

    void delete(UUID id);

    Product publish(UUID id);

    Product unpublish(UUID id);

    Product archive(UUID id);
}
