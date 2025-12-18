package com.sportsaas.catalog.api;

import com.sportsaas.catalog.api.dto.CreateProductRequest;
import com.sportsaas.catalog.api.dto.ProductResponse;
import com.sportsaas.catalog.api.dto.UpdateProductRequest;
import com.sportsaas.catalog.domain.*;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Gestion des produits")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CatalogMapper catalogMapper;

    @GetMapping
    @Operation(summary = "Lister les produits")
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<Product> page;

        if (search != null && !search.isBlank()) {
            page = productService.search(search, pageable);
        } else if (categoryId != null) {
            page = productService.findByCategory(categoryId, pageable);
        } else if (status != null) {
            page = productService.findByStatus(status, pageable);
        } else {
            page = productService.findAll(pageable);
        }

        return ResponseEntity.ok(PageResponse.of(page, catalogMapper::toProductResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un produit par ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
        return ResponseEntity.ok(catalogMapper.toProductResponse(product));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Recuperer un produit par SKU")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        Product product = productService.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("Product with SKU: " + sku));
        return ResponseEntity.ok(catalogMapper.toProductResponse(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Recuperer un produit par slug")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        Product product = productService.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product with slug: " + slug));
        return ResponseEntity.ok(catalogMapper.toProductResponse(product));
    }

    @PostMapping
    @Operation(summary = "Creer un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = catalogMapper.toProduct(request);

        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            product.setCategory(category);
        }

        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogMapper.toProductResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateProductRequest request) {
        Product existing = productService.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));

        catalogMapper.updateProduct(request, existing);

        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            existing.setCategory(category);
        } else {
            existing.setCategory(null);
        }

        Product updated = productService.update(id, existing);
        return ResponseEntity.ok(catalogMapper.toProductResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publier un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> publish(@PathVariable UUID id) {
        Product published = productService.publish(id);
        return ResponseEntity.ok(catalogMapper.toProductResponse(published));
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Depublier un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponse> unpublish(@PathVariable UUID id) {
        Product unpublished = productService.unpublish(id);
        return ResponseEntity.ok(catalogMapper.toProductResponse(unpublished));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archiver un produit")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ProductResponse> archive(@PathVariable UUID id) {
        Product archived = productService.archive(id);
        return ResponseEntity.ok(catalogMapper.toProductResponse(archived));
    }
}
