package com.sportsaas.catalog.infra;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.catalog.domain.ProductService;
import com.sportsaas.catalog.domain.ProductStatus;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.util.StringUtils;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service Product.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Product> search(String query, Pageable pageable) {
        return productRepository.search(query, pageable);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Product create(Product product) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        product.setTenantId(tenantId);

        // Check SKU uniqueness
        if (productRepository.existsBySku(product.getSku())) {
            throw new ConflictException("Un produit avec ce SKU existe deja");
        }

        // Generate slug if not provided
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(StringUtils.slugify(product.getName()));
        }

        product.setStatus(ProductStatus.DRAFT);

        Product saved = productRepository.save(product);
        log.info("Product created: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Product update(UUID id, Product product) {
        Product existing = findByIdOrThrow(id);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setShortDescription(product.getShortDescription());
        existing.setPrice(product.getPrice());
        existing.setCompareAtPrice(product.getCompareAtPrice());
        existing.setCostPrice(product.getCostPrice());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        existing.setBarcode(product.getBarcode());
        existing.setTaxable(product.isTaxable());
        existing.setWeight(product.getWeight());
        existing.setWeightUnit(product.getWeightUnit());

        Product saved = productRepository.save(existing);
        log.info("Product updated: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = findByIdOrThrow(id);
        productRepository.delete(product);
        log.info("Product deleted: {} ({})", product.getName(), product.getId());
    }

    @Override
    @Transactional
    public Product publish(UUID id) {
        Product product = findByIdOrThrow(id);
        product.setStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);
        log.info("Product published: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Product unpublish(UUID id) {
        Product product = findByIdOrThrow(id);
        product.setStatus(ProductStatus.INACTIVE);
        Product saved = productRepository.save(product);
        log.info("Product unpublished: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Product archive(UUID id) {
        Product product = findByIdOrThrow(id);
        product.setStatus(ProductStatus.ARCHIVED);
        Product saved = productRepository.save(product);
        log.info("Product archived: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    private Product findByIdOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }
}
