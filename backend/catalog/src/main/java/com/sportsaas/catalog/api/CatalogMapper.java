package com.sportsaas.catalog.api;

import com.sportsaas.catalog.api.dto.*;
import com.sportsaas.catalog.domain.Category;
import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductAttribute;
import com.sportsaas.catalog.domain.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "depth", expression = "java(category.getDepth())")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toCategoryResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "active", constant = "true")
    Category toCategory(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateCategory(UpdateCategoryRequest request, @MappingTarget Category category);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "onSale", expression = "java(product.isOnSale())")
    @Mapping(target = "discountPercentage", expression = "java(product.getDiscountPercentage())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImage() != null ? product.getMainImage().getUrl() : null)")
    ProductResponse toProductResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);

    ProductImageResponse toProductImageResponse(ProductImage image);

    ProductAttributeResponse toProductAttributeResponse(ProductAttribute attribute);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    Product toProduct(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    void updateProduct(UpdateProductRequest request, @MappingTarget Product product);
}
