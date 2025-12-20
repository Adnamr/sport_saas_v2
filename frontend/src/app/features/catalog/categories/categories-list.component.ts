import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CatalogService } from '../services/catalog.service';
import { Category } from '../models/catalog.model';
import { CategoryTreeItemComponent } from './category-tree-item.component';
import { CategoryModalComponent } from './category-modal.component';

@Component({
  selector: 'app-categories-list',
  standalone: true,
  imports: [CommonModule, CategoryTreeItemComponent, CategoryModalComponent],
  template: `
    <div>
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-secondary-900">Categories</h1>
        <button
          (click)="openCreateModal()"
          class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors flex items-center gap-2"
        >
          <span>+</span>
          <span>Nouvelle categorie</span>
        </button>
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
              (click)="loadCategories()"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Reessayer
            </button>
          </div>
        </div>
      }

      <!-- Loading State -->
      @if (isLoading()) {
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="space-y-4">
            @for (i of [1, 2, 3, 4]; track i) {
              <div class="animate-pulse flex items-center gap-3">
                <div class="w-6 h-6 bg-secondary-200 rounded"></div>
                <div class="h-4 bg-secondary-200 rounded w-48"></div>
              </div>
            }
          </div>
        </div>
      } @else {
        <!-- Categories Tree -->
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200">
          @if (categories().length === 0) {
            <div class="p-12 text-center">
              <div class="text-4xl mb-4">📁</div>
              <h3 class="text-lg font-medium text-secondary-900 mb-2">Aucune categorie</h3>
              <p class="text-secondary-600 mb-4">Commencez par creer votre premiere categorie</p>
              <button
                (click)="openCreateModal()"
                class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                Creer une categorie
              </button>
            </div>
          } @else {
            <div class="p-4">
              @for (category of categories(); track category.id) {
                <app-category-tree-item
                  [category]="category"
                  [level]="0"
                  (edit)="openEditModal($event)"
                  (delete)="confirmDelete($event)"
                  (toggleStatus)="toggleCategoryStatus($event)"
                  (addChild)="openCreateModal($event)"
                />
              }
            </div>
          }
        </div>
      }

      <!-- Category Modal -->
      @if (showModal()) {
        <app-category-modal
          [category]="editingCategory()"
          [parentCategory]="parentCategory()"
          (save)="saveCategory($event)"
          (close)="closeModal()"
        />
      }

      <!-- Delete Confirmation Modal -->
      @if (showDeleteModal()) {
        <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
            <h3 class="text-lg font-semibold text-secondary-900 mb-4">Supprimer la categorie</h3>
            <p class="text-secondary-600 mb-6">
              Etes-vous sur de vouloir supprimer la categorie "{{ deletingCategory()?.name }}" ?
              Cette action est irreversible.
            </p>
            <div class="flex justify-end gap-3">
              <button
                (click)="closeDeleteModal()"
                class="px-4 py-2 border border-secondary-300 text-secondary-700 rounded-lg hover:bg-secondary-50 transition-colors"
              >
                Annuler
              </button>
              <button
                (click)="deleteCategory()"
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
export class CategoriesListComponent implements OnInit {
  private readonly catalogService = inject(CatalogService);

  categories = signal<Category[]>([]);
  isLoading = signal(true);
  error = signal<string | null>(null);
  saveError = signal<string | null>(null);

  showModal = signal(false);
  editingCategory = signal<Category | null>(null);
  parentCategory = signal<Category | null>(null);

  showDeleteModal = signal(false);
  deletingCategory = signal<Category | null>(null);

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.catalogService.getRootCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.error.set('Erreur lors du chargement des categories');
        this.isLoading.set(false);
      },
    });
  }

  openCreateModal(parent?: Category): void {
    this.editingCategory.set(null);
    this.parentCategory.set(parent || null);
    this.showModal.set(true);
  }

  openEditModal(category: Category): void {
    this.editingCategory.set(category);
    this.parentCategory.set(null);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.editingCategory.set(null);
    this.parentCategory.set(null);
  }

  saveCategory(data: { name: string; description?: string; parentId?: string }): void {
    const category = this.editingCategory();
    this.saveError.set(null);

    if (category) {
      // Update
      this.catalogService.updateCategory(category.id, data).subscribe({
        next: () => {
          this.closeModal();
          this.loadCategories();
        },
        error: () => {
          this.closeModal();
          this.saveError.set('Erreur lors de la mise a jour de la categorie');
        },
      });
    } else {
      // Create
      this.catalogService.createCategory(data).subscribe({
        next: () => {
          this.closeModal();
          this.loadCategories();
        },
        error: () => {
          this.closeModal();
          this.saveError.set('Erreur lors de la creation de la categorie');
        },
      });
    }
  }

  confirmDelete(category: Category): void {
    this.deletingCategory.set(category);
    this.showDeleteModal.set(true);
  }

  closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.deletingCategory.set(null);
  }

  deleteCategory(): void {
    const category = this.deletingCategory();
    if (!category) return;

    this.saveError.set(null);
    this.catalogService.deleteCategory(category.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.loadCategories();
      },
      error: () => {
        this.closeDeleteModal();
        this.saveError.set('Erreur lors de la suppression de la categorie');
      },
    });
  }

  toggleCategoryStatus(category: Category): void {
    const action = category.active
      ? this.catalogService.deactivateCategory(category.id)
      : this.catalogService.activateCategory(category.id);

    this.saveError.set(null);
    action.subscribe({
      next: () => {
        this.loadCategories();
      },
      error: () => {
        this.saveError.set('Erreur lors du changement de statut de la categorie');
      },
    });
  }
}
