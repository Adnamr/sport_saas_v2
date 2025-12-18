package com.sportsaas.catalog.infra;

import com.sportsaas.catalog.domain.Category;
import com.sportsaas.catalog.domain.CategoryRepository;
import com.sportsaas.catalog.domain.CategoryService;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.util.StringUtils;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation du service Category.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> findRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return categoryRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Category create(Category category) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        category.setTenantId(tenantId);

        // Validate no cycle in parent hierarchy
        validateNoCycle(category, category.getParent());

        // Generate slug if not provided
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(StringUtils.slugify(category.getName()));
        }

        // Check slug uniqueness
        if (categoryRepository.existsBySlug(category.getSlug())) {
            throw new ConflictException("Une categorie avec ce slug existe deja");
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Category update(UUID id, Category category) {
        Category existing = findByIdOrThrow(id);

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setImageUrl(category.getImageUrl());
        existing.setSortOrder(category.getSortOrder());

        Category saved = categoryRepository.save(existing);
        log.info("Category updated: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = findByIdOrThrow(id);
        categoryRepository.delete(category);
        log.info("Category deleted: {} ({})", category.getName(), category.getId());
    }

    @Override
    @Transactional
    public Category activate(UUID id) {
        Category category = findByIdOrThrow(id);
        category.setActive(true);
        Category saved = categoryRepository.save(category);
        log.info("Category activated: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Category deactivate(UUID id) {
        Category category = findByIdOrThrow(id);
        category.setActive(false);
        Category saved = categoryRepository.save(category);
        log.info("Category deactivated: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    private Category findByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    /**
     * Validates that setting a parent won't create a cycle in the category hierarchy.
     * A cycle would occur if the parent (or any of its ancestors) is the category itself.
     */
    private void validateNoCycle(Category category, Category parent) {
        if (parent == null) {
            return;
        }

        Category current = parent;
        while (current != null) {
            if (current.getId() != null && current.getId().equals(category.getId())) {
                throw new BadRequestException("Une categorie ne peut pas etre son propre parent (cycle detecte)");
            }
            current = current.getParent();
        }
    }
}
