import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../services/catalog.service';
import { Product, ProductStatus, Category } from '../models/catalog.model';

@Component({
  selector: 'app-products-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div>
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-secondary-900">Produits</h1>
        <a
          routerLink="new"
          class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors flex items-center gap-2"
        >
          <span>+</span>
          <span>Nouveau produit</span>
        </a>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-4 mb-6">
        <div class="flex flex-wrap gap-4">
          <!-- Search -->
          <div class="flex-1 min-w-64">
            <input
              type="text"
              [(ngModel)]="searchQuery"
              (input)="onSearchChange()"
              class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
              placeholder="Rechercher un produit..."
            />
          </div>

          <!-- Category Filter -->
          <select
            [(ngModel)]="selectedCategoryId"
            (change)="loadProducts()"
            class="px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
          >
            <option value="">Toutes les categories</option>
            @for (category of categories(); track category.id) {
              <option [value]="category.id">{{ category.name }}</option>
            }
          </select>

          <!-- Status Filter -->
          <select
            [(ngModel)]="selectedStatus"
            (change)="loadProducts()"
            class="px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
          >
            <option value="">Tous les statuts</option>
            <option value="DRAFT">Brouillon</option>
            <option value="ACTIVE">Actif</option>
            <option value="INACTIVE">Inactif</option>
            <option value="ARCHIVED">Archive</option>
          </select>
        </div>
      </div>

      <!-- Save Error -->
      @if (saveError()) {
        <div class="bg-red-50 border border-red-200 rounded-xl p-4 mb-6">
          <div class="flex items-center gap-3">
            <span class="text-xl">⚠️</span>
            <p class="text-red-800 font-medium flex-1">{{ saveError() }}</p>
            <button
              (click)="saveError.set(null)"
              class="text-red-600 hover:text-red-800"
            >
              &times;
            </button>
          </div>
        </div>
      }

      <!-- Error State -->
      @if (error()) {
        <div class="bg-red-50 border border-red-200 rounded-xl p-6 mb-6">
          <div class="flex items-center gap-3">
            <span class="text-2xl">⚠️</span>
            <div class="flex-1">
              <p class="text-red-800 font-medium">{{ error() }}</p>
            </div>
            <button
              (click)="loadProducts()"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Reessayer
            </button>
          </div>
        </div>
      }

      <!-- Loading State -->
      @if (isLoading()) {
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200">
          <div class="divide-y divide-secondary-200">
            @for (i of [1, 2, 3, 4, 5]; track i) {
              <div class="p-4 animate-pulse flex items-center gap-4">
                <div class="w-16 h-16 bg-secondary-200 rounded-lg"></div>
                <div class="flex-1">
                  <div class="h-4 bg-secondary-200 rounded w-48 mb-2"></div>
                  <div class="h-3 bg-secondary-200 rounded w-32"></div>
                </div>
                <div class="h-6 bg-secondary-200 rounded w-20"></div>
              </div>
            }
          </div>
        </div>
      } @else {
        <!-- Products Table -->
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 overflow-hidden">
          @if (products().length === 0) {
            <div class="p-12 text-center">
              <div class="text-4xl mb-4">📦</div>
              <h3 class="text-lg font-medium text-secondary-900 mb-2">Aucun produit</h3>
              <p class="text-secondary-600 mb-4">Commencez par creer votre premier produit</p>
              <a
                routerLink="new"
                class="inline-flex px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                Creer un produit
              </a>
            </div>
          } @else {
            <table class="w-full">
              <thead class="bg-secondary-50">
                <tr>
                  <th class="px-4 py-3 text-left text-sm font-medium text-secondary-700">Produit</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-secondary-700">SKU</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-secondary-700">Categorie</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-secondary-700">Prix</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-secondary-700">Statut</th>
                  <th class="px-4 py-3 text-right text-sm font-medium text-secondary-700">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-secondary-200">
                @for (product of products(); track product.id) {
                  <tr class="hover:bg-secondary-50 transition-colors">
                    <td class="px-4 py-4">
                      <div class="flex items-center gap-3">
                        @if (product.mainImageUrl) {
                          <img
                            [src]="product.mainImageUrl"
                            [alt]="product.name"
                            class="w-12 h-12 rounded-lg object-cover"
                          />
                        } @else {
                          <div class="w-12 h-12 rounded-lg bg-secondary-100 flex items-center justify-center">
                            <span class="text-xl">📦</span>
                          </div>
                        }
                        <div>
                          <p class="font-medium text-secondary-900">{{ product.name }}</p>
                          @if (product.brand) {
                            <p class="text-sm text-secondary-500">{{ product.brand }}</p>
                          }
                        </div>
                      </div>
                    </td>
                    <td class="px-4 py-4">
                      <span class="text-sm font-mono text-secondary-600">{{ product.sku }}</span>
                    </td>
                    <td class="px-4 py-4">
                      <span class="text-sm text-secondary-600">{{ product.categoryName || '-' }}</span>
                    </td>
                    <td class="px-4 py-4">
                      <div>
                        <span class="font-medium text-secondary-900">{{ formatPrice(product.price) }}</span>
                        @if (product.compareAtPrice && product.compareAtPrice > product.price) {
                          <span class="text-sm text-secondary-500 line-through ml-2">
                            {{ formatPrice(product.compareAtPrice) }}
                          </span>
                        }
                      </div>
                    </td>
                    <td class="px-4 py-4">
                      <span
                        class="px-2 py-1 text-xs font-medium rounded-full"
                        [ngClass]="getStatusClass(product.status)"
                      >
                        {{ getStatusLabel(product.status) }}
                      </span>
                    </td>
                    <td class="px-4 py-4 text-right">
                      <div class="flex items-center justify-end gap-1">
                        <a
                          [routerLink]="[product.id]"
                          class="p-2 text-secondary-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                          title="Modifier"
                        >
                          <span class="text-sm">✏️</span>
                        </a>
                        @if (product.status === 'DRAFT') {
                          <button
                            (click)="publishProduct(product)"
                            class="p-2 text-secondary-500 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                            title="Publier"
                          >
                            <span class="text-sm">🚀</span>
                          </button>
                        }
                        @if (product.status === 'ACTIVE') {
                          <button
                            (click)="unpublishProduct(product)"
                            class="p-2 text-secondary-500 hover:text-orange-600 hover:bg-orange-50 rounded-lg transition-colors"
                            title="Depublier"
                          >
                            <span class="text-sm">⏸️</span>
                          </button>
                        }
                        <button
                          (click)="confirmDelete(product)"
                          class="p-2 text-secondary-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Supprimer"
                        >
                          <span class="text-sm">🗑️</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>

            <!-- Pagination -->
            @if (totalPages() > 1) {
              <div class="flex items-center justify-between px-4 py-3 border-t border-secondary-200">
                <p class="text-sm text-secondary-600">
                  Page {{ currentPage() + 1 }} sur {{ totalPages() }} ({{ totalElements() }} produits)
                </p>
                <div class="flex gap-2">
                  <button
                    (click)="goToPage(currentPage() - 1)"
                    [disabled]="currentPage() === 0"
                    class="px-3 py-1 border border-secondary-300 rounded-lg text-sm hover:bg-secondary-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    Precedent
                  </button>
                  <button
                    (click)="goToPage(currentPage() + 1)"
                    [disabled]="currentPage() >= totalPages() - 1"
                    class="px-3 py-1 border border-secondary-300 rounded-lg text-sm hover:bg-secondary-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    Suivant
                  </button>
                </div>
              </div>
            }
          }
        </div>
      }

      <!-- Delete Modal -->
      @if (showDeleteModal()) {
        <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
            <h3 class="text-lg font-semibold text-secondary-900 mb-4">Supprimer le produit</h3>
            <p class="text-secondary-600 mb-6">
              Etes-vous sur de vouloir supprimer "{{ deletingProduct()?.name }}" ?
            </p>
            <div class="flex justify-end gap-3">
              <button
                (click)="closeDeleteModal()"
                class="px-4 py-2 border border-secondary-300 text-secondary-700 rounded-lg hover:bg-secondary-50 transition-colors"
              >
                Annuler
              </button>
              <button
                (click)="deleteProduct()"
                class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
              >
                Supprimer
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class ProductsListComponent implements OnInit, OnDestroy {
  private readonly catalogService = inject(CatalogService);

  products = signal<Product[]>([]);
  categories = signal<Category[]>([]);
  isLoading = signal(true);
  error = signal<string | null>(null);
  saveError = signal<string | null>(null);

  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  searchQuery = '';
  selectedCategoryId = '';
  selectedStatus = '';

  showDeleteModal = signal(false);
  deletingProduct = signal<Product | null>(null);

  private searchTimeout: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  ngOnDestroy(): void {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
  }

  loadCategories(): void {
    this.catalogService.getRootCategories().subscribe({
      next: (categories) => {
        this.categories.set(this.flattenCategories(categories));
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      },
    });
  }

  private flattenCategories(categories: Category[], result: Category[] = []): Category[] {
    for (const cat of categories) {
      result.push(cat);
      if (cat.children) {
        this.flattenCategories(cat.children, result);
      }
    }
    return result;
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.error.set(null);

    const params: Record<string, string | number> = {
      page: this.currentPage(),
      size: this.pageSize,
    };

    if (this.searchQuery) params['search'] = this.searchQuery;
    if (this.selectedCategoryId) params['categoryId'] = this.selectedCategoryId;
    if (this.selectedStatus) params['status'] = this.selectedStatus;

    this.catalogService.getProducts(params as any).subscribe({
      next: (response) => {
        this.products.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.error.set('Erreur lors du chargement des produits');
        this.isLoading.set(false);
      },
    });
  }

  onSearchChange(): void {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.loadProducts();
    }, 300);
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadProducts();
    }
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
    }).format(price);
  }

  getStatusClass(status: ProductStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'DRAFT':
        return 'bg-yellow-100 text-yellow-800';
      case 'INACTIVE':
        return 'bg-secondary-100 text-secondary-800';
      case 'ARCHIVED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-secondary-100 text-secondary-800';
    }
  }

  getStatusLabel(status: ProductStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'Actif';
      case 'DRAFT':
        return 'Brouillon';
      case 'INACTIVE':
        return 'Inactif';
      case 'ARCHIVED':
        return 'Archive';
      default:
        return status;
    }
  }

  publishProduct(product: Product): void {
    this.saveError.set(null);
    this.catalogService.publishProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: () => {
        this.saveError.set('Erreur lors de la publication du produit');
      },
    });
  }

  unpublishProduct(product: Product): void {
    this.saveError.set(null);
    this.catalogService.unpublishProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: () => {
        this.saveError.set('Erreur lors de la depublication du produit');
      },
    });
  }

  confirmDelete(product: Product): void {
    this.deletingProduct.set(product);
    this.showDeleteModal.set(true);
  }

  closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.deletingProduct.set(null);
  }

  deleteProduct(): void {
    const product = this.deletingProduct();
    if (!product) return;

    this.catalogService.deleteProduct(product.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.loadProducts();
      },
      error: () => {
        this.closeDeleteModal();
        this.saveError.set('Erreur lors de la suppression du produit');
      },
    });
  }
}
