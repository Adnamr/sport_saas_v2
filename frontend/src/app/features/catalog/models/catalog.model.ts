// Category Models
export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  sortOrder: number;
  active: boolean;
  depth: number;
  children?: Category[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  slug?: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  sortOrder?: number;
}

export interface UpdateCategoryRequest {
  name?: string;
  slug?: string;
  description?: string;
  imageUrl?: string;
  sortOrder?: number;
}

// Product Models
export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';

export interface Product {
  id: string;
  name: string;
  sku: string;
  slug: string;
  description?: string;
  shortDescription?: string;
  price: number;
  compareAtPrice?: number;
  status: ProductStatus;
  categoryId?: string;
  categoryName?: string;
  brand?: string;
  barcode?: string;
  taxable: boolean;
  weight?: number;
  weightUnit?: string;
  onSale: boolean;
  discountPercentage: number;
  mainImageUrl?: string;
  images?: ProductImage[];
  attributes?: ProductAttribute[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductImage {
  id: string;
  url: string;
  altText?: string;
  sortOrder: number;
  isMain: boolean;
}

export interface ProductAttribute {
  id: string;
  name: string;
  value: string;
}

export interface CreateProductRequest {
  name: string;
  sku: string;
  slug?: string;
  description?: string;
  shortDescription?: string;
  price: number;
  compareAtPrice?: number;
  categoryId?: string;
  brand?: string;
  barcode?: string;
  taxable?: boolean;
  weight?: number;
  weightUnit?: string;
}

export interface UpdateProductRequest {
  name?: string;
  sku?: string;
  slug?: string;
  description?: string;
  shortDescription?: string;
  price?: number;
  compareAtPrice?: number;
  categoryId?: string;
  brand?: string;
  barcode?: string;
  taxable?: boolean;
  weight?: number;
  weightUnit?: string;
}
