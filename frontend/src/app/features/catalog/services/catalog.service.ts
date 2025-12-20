import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { PageResponse } from '../../../core/models';
import {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  Product,
  ProductStatus,
  CreateProductRequest,
  UpdateProductRequest,
} from '../models/catalog.model';

@Injectable({
  providedIn: 'root',
})
export class CatalogService {
  private readonly api = inject(ApiService);

  // ==================== Categories ====================

  getRootCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }

  getCategoryById(id: string): Observable<Category> {
    return this.api.get<Category>(`/categories/${id}`);
  }

  getCategoryChildren(id: string): Observable<Category[]> {
    return this.api.get<Category[]>(`/categories/${id}/children`);
  }

  getCategoryBySlug(slug: string): Observable<Category> {
    return this.api.get<Category>(`/categories/slug/${slug}`);
  }

  createCategory(data: CreateCategoryRequest): Observable<Category> {
    return this.api.post<Category>('/categories', data);
  }

  updateCategory(id: string, data: UpdateCategoryRequest): Observable<Category> {
    return this.api.put<Category>(`/categories/${id}`, data);
  }

  deleteCategory(id: string): Observable<void> {
    return this.api.delete<void>(`/categories/${id}`);
  }

  activateCategory(id: string): Observable<Category> {
    return this.api.post<Category>(`/categories/${id}/activate`, {});
  }

  deactivateCategory(id: string): Observable<Category> {
    return this.api.post<Category>(`/categories/${id}/deactivate`, {});
  }

  // ==================== Products ====================

  getProducts(params?: {
    categoryId?: string;
    status?: ProductStatus;
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PageResponse<Product>> {
    return this.api.getPage<Product>('/products', params);
  }

  getProductById(id: string): Observable<Product> {
    return this.api.get<Product>(`/products/${id}`);
  }

  getProductBySku(sku: string): Observable<Product> {
    return this.api.get<Product>(`/products/sku/${sku}`);
  }

  getProductBySlug(slug: string): Observable<Product> {
    return this.api.get<Product>(`/products/slug/${slug}`);
  }

  createProduct(data: CreateProductRequest): Observable<Product> {
    return this.api.post<Product>('/products', data);
  }

  updateProduct(id: string, data: UpdateProductRequest): Observable<Product> {
    return this.api.put<Product>(`/products/${id}`, data);
  }

  deleteProduct(id: string): Observable<void> {
    return this.api.delete<void>(`/products/${id}`);
  }

  publishProduct(id: string): Observable<Product> {
    return this.api.post<Product>(`/products/${id}/publish`, {});
  }

  unpublishProduct(id: string): Observable<Product> {
    return this.api.post<Product>(`/products/${id}/unpublish`, {});
  }

  archiveProduct(id: string): Observable<Product> {
    return this.api.post<Product>(`/products/${id}/archive`, {});
  }
}
