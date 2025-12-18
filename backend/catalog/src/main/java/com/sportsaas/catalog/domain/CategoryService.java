package com.sportsaas.catalog.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Category.
 */
public interface CategoryService {

    List<Category> findAll();

    List<Category> findRootCategories();

    List<Category> findByParentId(UUID parentId);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    Category create(Category category);

    Category update(UUID id, Category category);

    void delete(UUID id);

    Category activate(UUID id);

    Category deactivate(UUID id);
}
