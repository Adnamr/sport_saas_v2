import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CatalogService } from '../services/catalog.service';
import { Product, Category } from '../models/catalog.model';

type TabId = 'info' | 'pricing' | 'stock' | 'media' | 'attributes' | 'seo';

interface Tab {
  id: TabId;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div>
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <a
            routerLink="../"
            class="p-2 text-secondary-500 hover:text-secondary-700 hover:bg-secondary-100 rounded-lg transition-colors"
          >
            <span class="text-xl">&larr;</span>
          </a>
          <h1 class="text-2xl font-bold text-secondary-900">
            {{ isEditMode() ? 'Modifier le produit' : 'Nouveau produit' }}
          </h1>
        </div>
        <div class="flex items-center gap-3">
          @if (isEditMode() && product()) {
            @if (product()!.status === 'DRAFT') {
              <button
                (click)="publish()"
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                Publier
              </button>
            }
          }
          <button
            (click)="save()"
            [disabled]="form.invalid || isSaving()"
            class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-secondary-300 disabled:cursor-not-allowed transition-colors"
          >
            {{ isSaving() ? 'Enregistrement...' : 'Enregistrer' }}
          </button>
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

      <!-- Loading State -->
      @if (isLoading()) {
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="animate-pulse space-y-4">
            <div class="h-8 bg-secondary-200 rounded w-48"></div>
            <div class="h-4 bg-secondary-200 rounded w-full"></div>
            <div class="h-4 bg-secondary-200 rounded w-3/4"></div>
          </div>
        </div>
      } @else {
        <form [formGroup]="form">
          <div class="flex gap-6">
            <!-- Main Content -->
            <div class="flex-1">
              <!-- Tabs -->
              <div class="bg-white rounded-xl shadow-sm border border-secondary-200 overflow-hidden">
                <div class="border-b border-secondary-200">
                  <nav class="flex -mb-px">
                    @for (tab of tabs; track tab.id) {
                      <button
                        type="button"
                        (click)="activeTab.set(tab.id)"
                        class="px-4 py-3 text-sm font-medium border-b-2 transition-colors"
                        [ngClass]="{
                          'border-primary-500 text-primary-600': activeTab() === tab.id,
                          'border-transparent text-secondary-500 hover:text-secondary-700 hover:border-secondary-300': activeTab() !== tab.id
                        }"
                      >
                        <span class="mr-2">{{ tab.icon }}</span>
                        {{ tab.label }}
                      </button>
                    }
                  </nav>
                </div>

                <div class="p-6">
                  <!-- Info Tab -->
                  @if (activeTab() === 'info') {
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          Nom <span class="text-red-500">*</span>
                        </label>
                        <input
                          type="text"
                          formControlName="name"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                          placeholder="Nom du produit"
                        />
                      </div>

                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            SKU <span class="text-red-500">*</span>
                          </label>
                          <input
                            type="text"
                            formControlName="sku"
                            class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            placeholder="SKU-001"
                          />
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            Code-barres
                          </label>
                          <input
                            type="text"
                            formControlName="barcode"
                            class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            placeholder="1234567890123"
                          />
                        </div>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          Description courte
                        </label>
                        <textarea
                          formControlName="shortDescription"
                          rows="2"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                          placeholder="Breve description du produit"
                        ></textarea>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          Description complete
                        </label>
                        <textarea
                          formControlName="description"
                          rows="5"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none"
                          placeholder="Description detaillee du produit"
                        ></textarea>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          Marque
                        </label>
                        <input
                          type="text"
                          formControlName="brand"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                          placeholder="Nom de la marque"
                        />
                      </div>
                    </div>
                  }

                  <!-- Pricing Tab -->
                  @if (activeTab() === 'pricing') {
                    <div class="space-y-4">
                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            Prix <span class="text-red-500">*</span>
                          </label>
                          <div class="relative">
                            <input
                              type="number"
                              formControlName="price"
                              step="0.01"
                              min="0"
                              class="w-full px-4 py-2 pr-12 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                              placeholder="0.00"
                            />
                            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-secondary-500">EUR</span>
                          </div>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            Prix compare (barre)
                          </label>
                          <div class="relative">
                            <input
                              type="number"
                              formControlName="compareAtPrice"
                              step="0.01"
                              min="0"
                              class="w-full px-4 py-2 pr-12 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                              placeholder="0.00"
                            />
                            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-secondary-500">EUR</span>
                          </div>
                        </div>
                      </div>

                      <div class="flex items-center gap-3">
                        <input
                          type="checkbox"
                          formControlName="taxable"
                          id="taxable"
                          class="w-4 h-4 rounded border-secondary-300 text-primary-600 focus:ring-primary-500"
                        />
                        <label for="taxable" class="text-sm text-secondary-700">
                          Produit taxable
                        </label>
                      </div>
                    </div>
                  }

                  <!-- Stock Tab -->
                  @if (activeTab() === 'stock') {
                    <div class="space-y-4">
                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            Poids
                          </label>
                          <input
                            type="number"
                            formControlName="weight"
                            step="0.01"
                            min="0"
                            class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            placeholder="0.00"
                          />
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-secondary-700 mb-1">
                            Unite de poids
                          </label>
                          <select
                            formControlName="weightUnit"
                            class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                          >
                            <option value="">Selectionner</option>
                            <option value="kg">Kilogrammes (kg)</option>
                            <option value="g">Grammes (g)</option>
                            <option value="lb">Livres (lb)</option>
                          </select>
                        </div>
                      </div>

                      <div class="p-4 bg-secondary-50 rounded-lg">
                        <p class="text-sm text-secondary-600">
                          La gestion du stock se fait dans le module Inventaire.
                        </p>
                      </div>
                    </div>
                  }

                  <!-- Media Tab -->
                  @if (activeTab() === 'media') {
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          URL de l'image principale
                        </label>
                        <input
                          type="text"
                          formControlName="mainImageUrl"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                          placeholder="https://..."
                        />
                      </div>

                      @if (form.get('mainImageUrl')?.value) {
                        <div class="mt-4">
                          <img
                            [src]="form.get('mainImageUrl')?.value"
                            alt="Apercu"
                            class="max-w-xs rounded-lg border border-secondary-200"
                          />
                        </div>
                      }

                      <div class="p-4 bg-secondary-50 rounded-lg">
                        <p class="text-sm text-secondary-600">
                          L'upload d'images sera disponible prochainement.
                        </p>
                      </div>
                    </div>
                  }

                  <!-- Attributes Tab -->
                  @if (activeTab() === 'attributes') {
                    <div class="p-8 text-center">
                      <div class="text-4xl mb-4">🏷️</div>
                      <h3 class="text-lg font-medium text-secondary-900 mb-2">Attributs</h3>
                      <p class="text-secondary-600">
                        La gestion des attributs sera disponible prochainement.
                      </p>
                    </div>
                  }

                  <!-- SEO Tab -->
                  @if (activeTab() === 'seo') {
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-secondary-700 mb-1">
                          Slug (URL)
                        </label>
                        <input
                          type="text"
                          formControlName="slug"
                          class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                          placeholder="mon-produit"
                        />
                        <p class="mt-1 text-xs text-secondary-500">
                          Laissez vide pour generer automatiquement
                        </p>
                      </div>
                    </div>
                  }
                </div>
              </div>
            </div>

            <!-- Sidebar -->
            <div class="w-80 space-y-6">
              <!-- Status -->
              <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-4">
                <h3 class="font-medium text-secondary-900 mb-3">Statut</h3>
                @if (isEditMode() && product()) {
                  <span
                    class="px-3 py-1 text-sm font-medium rounded-full"
                    [ngClass]="getStatusClass(product()!.status)"
                  >
                    {{ getStatusLabel(product()!.status) }}
                  </span>
                } @else {
                  <span class="px-3 py-1 text-sm font-medium rounded-full bg-yellow-100 text-yellow-800">
                    Brouillon
                  </span>
                }
              </div>

              <!-- Category -->
              <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-4">
                <h3 class="font-medium text-secondary-900 mb-3">Categorie</h3>
                <select
                  formControlName="categoryId"
                  class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                >
                  <option value="">Aucune categorie</option>
                  @for (category of categories(); track category.id) {
                    <option [value]="category.id">
                      {{ getCategoryPrefix(category) }}{{ category.name }}
                    </option>
                  }
                </select>
              </div>
            </div>
          </div>
        </form>
      }
    </div>
  `,
})
export class ProductFormComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly fb = inject(FormBuilder);

  tabs: Tab[] = [
    { id: 'info', label: 'Informations', icon: '📋' },
    { id: 'pricing', label: 'Prix', icon: '💰' },
    { id: 'stock', label: 'Stock', icon: '📦' },
    { id: 'media', label: 'Medias', icon: '🖼️' },
    { id: 'attributes', label: 'Attributs', icon: '🏷️' },
    { id: 'seo', label: 'SEO', icon: '🔍' },
  ];

  activeTab = signal<TabId>('info');
  isEditMode = signal(false);
  isLoading = signal(false);
  isSaving = signal(false);
  saveError = signal<string | null>(null);
  product = signal<Product | null>(null);
  categories = signal<Category[]>([]);

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    sku: ['', Validators.required],
    slug: [''],
    description: [''],
    shortDescription: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    compareAtPrice: [null],
    categoryId: [''],
    brand: [''],
    barcode: [''],
    taxable: [true],
    weight: [null],
    weightUnit: [''],
    mainImageUrl: [''],
  });

  ngOnInit(): void {
    this.loadCategories();

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode.set(true);
      this.loadProduct(id);
    }
  }

  loadCategories(): void {
    this.catalogService.getRootCategories().subscribe({
      next: (categories) => {
        this.categories.set(this.flattenCategories(categories));
      },
      error: (err) => console.error('Error loading categories:', err),
    });
  }

  private flattenCategories(
    categories: Category[],
    result: Category[] = [],
    depth = 0
  ): Category[] {
    for (const cat of categories) {
      result.push({ ...cat, depth });
      if (cat.children) {
        this.flattenCategories(cat.children, result, depth + 1);
      }
    }
    return result;
  }

  getCategoryPrefix(category: Category): string {
    return '—'.repeat(category.depth) + (category.depth > 0 ? ' ' : '');
  }

  loadProduct(id: string): void {
    this.isLoading.set(true);

    this.catalogService.getProductById(id).subscribe({
      next: (product) => {
        this.product.set(product);
        this.form.patchValue({
          name: product.name,
          sku: product.sku,
          slug: product.slug,
          description: product.description || '',
          shortDescription: product.shortDescription || '',
          price: product.price,
          compareAtPrice: product.compareAtPrice || null,
          categoryId: product.categoryId || '',
          brand: product.brand || '',
          barcode: product.barcode || '',
          taxable: product.taxable,
          weight: product.weight || null,
          weightUnit: product.weightUnit || '',
          mainImageUrl: product.mainImageUrl || '',
        });
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading product:', err);
        this.isLoading.set(false);
      },
    });
  }

  save(): void {
    if (this.form.invalid) return;

    this.isSaving.set(true);
    this.saveError.set(null);
    const formData = this.cleanFormData(this.form.value);

    const action = this.isEditMode()
      ? this.catalogService.updateProduct(this.product()!.id, formData as any)
      : this.catalogService.createProduct(formData as any);

    action.subscribe({
      next: (product) => {
        this.isSaving.set(false);
        if (!this.isEditMode()) {
          this.router.navigate(['../', product.id], { relativeTo: this.route });
        } else {
          this.product.set(product);
        }
      },
      error: () => {
        this.isSaving.set(false);
        this.saveError.set('Erreur lors de l\'enregistrement du produit');
      },
    });
  }

  private cleanFormData(data: Record<string, unknown>): Record<string, unknown> {
    const cleaned: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(data)) {
      if (value !== '' && value !== null && value !== undefined) {
        cleaned[key] = value;
      }
    }
    return cleaned;
  }

  publish(): void {
    if (!this.product()) return;

    this.catalogService.publishProduct(this.product()!.id).subscribe({
      next: (product) => {
        this.product.set(product);
      },
      error: (err) => console.error('Error publishing product:', err),
    });
  }

  getStatusClass(status: string): string {
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

  getStatusLabel(status: string): string {
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
}
