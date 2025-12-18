package com.sportsaas.catalog.api;

import com.sportsaas.catalog.api.dto.CategoryResponse;
import com.sportsaas.catalog.api.dto.CreateCategoryRequest;
import com.sportsaas.catalog.api.dto.UpdateCategoryRequest;
import com.sportsaas.catalog.domain.Category;
import com.sportsaas.catalog.domain.CategoryService;
import com.sportsaas.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Gestion des categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CatalogMapper catalogMapper;

    @GetMapping
    @Operation(summary = "Lister les categories racines")
    public ResponseEntity<List<CategoryResponse>> listRoots() {
        List<Category> categories = categoryService.findRootCategories();
        return ResponseEntity.ok(catalogMapper.toCategoryResponseList(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une categorie par ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
        return ResponseEntity.ok(catalogMapper.toCategoryResponse(category));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Lister les sous-categories")
    public ResponseEntity<List<CategoryResponse>> getChildren(@PathVariable UUID id) {
        List<Category> children = categoryService.findByParentId(id);
        return ResponseEntity.ok(catalogMapper.toCategoryResponseList(children));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Recuperer une categorie par slug")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        Category category = categoryService.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Category with slug: " + slug));
        return ResponseEntity.ok(catalogMapper.toCategoryResponse(category));
    }

    @PostMapping
    @Operation(summary = "Creer une categorie")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = catalogMapper.toCategory(request);

        if (request.getParentId() != null) {
            Category parent = categoryService.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent Category", request.getParentId()));
            category.setParent(parent);
        }

        Category created = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogMapper.toCategoryResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour une categorie")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateCategoryRequest request) {
        Category existing = categoryService.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
        catalogMapper.updateCategory(request, existing);
        Category updated = categoryService.update(id, existing);
        return ResponseEntity.ok(catalogMapper.toCategoryResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une categorie")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activer une categorie")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CategoryResponse> activate(@PathVariable UUID id) {
        Category activated = categoryService.activate(id);
        return ResponseEntity.ok(catalogMapper.toCategoryResponse(activated));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Desactiver une categorie")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CategoryResponse> deactivate(@PathVariable UUID id) {
        Category deactivated = categoryService.deactivate(id);
        return ResponseEntity.ok(catalogMapper.toCategoryResponse(deactivated));
    }
}
