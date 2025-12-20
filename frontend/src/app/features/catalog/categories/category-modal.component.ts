import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Category } from '../models/catalog.model';

@Component({
  selector: 'app-category-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b border-secondary-200">
          <h2 class="text-lg font-semibold text-secondary-900">
            {{ category ? 'Modifier la categorie' : 'Nouvelle categorie' }}
          </h2>
          <button
            (click)="close.emit()"
            class="p-2 text-secondary-500 hover:text-secondary-700 hover:bg-secondary-100 rounded-lg transition-colors"
          >
            <span class="text-xl">&times;</span>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="p-6">
          @if (parentCategory) {
            <div class="mb-4 p-3 bg-primary-50 border border-primary-200 rounded-lg">
              <p class="text-sm text-primary-800">
                Sous-categorie de: <strong>{{ parentCategory.name }}</strong>
              </p>
            </div>
          }

          <div class="space-y-4">
            <!-- Name -->
            <div>
              <label class="block text-sm font-medium text-secondary-700 mb-1">
                Nom <span class="text-red-500">*</span>
              </label>
              <input
                type="text"
                formControlName="name"
                class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                placeholder="Nom de la categorie"
              />
              @if (form.get('name')?.touched && form.get('name')?.hasError('required')) {
                <p class="mt-1 text-sm text-red-600">Le nom est requis</p>
              }
            </div>

            <!-- Slug -->
            <div>
              <label class="block text-sm font-medium text-secondary-700 mb-1">
                Slug
              </label>
              <input
                type="text"
                formControlName="slug"
                class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                placeholder="url-friendly-name"
              />
              <p class="mt-1 text-xs text-secondary-500">
                Laissez vide pour generer automatiquement
              </p>
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm font-medium text-secondary-700 mb-1">
                Description
              </label>
              <textarea
                formControlName="description"
                rows="3"
                class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors resize-none"
                placeholder="Description de la categorie"
              ></textarea>
            </div>

            <!-- Image URL -->
            <div>
              <label class="block text-sm font-medium text-secondary-700 mb-1">
                URL de l'image
              </label>
              <input
                type="text"
                formControlName="imageUrl"
                class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                placeholder="https://..."
              />
            </div>

            <!-- Sort Order -->
            <div>
              <label class="block text-sm font-medium text-secondary-700 mb-1">
                Ordre d'affichage
              </label>
              <input
                type="number"
                formControlName="sortOrder"
                class="w-full px-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
                placeholder="0"
              />
            </div>
          </div>

          <!-- Actions -->
          <div class="flex justify-end gap-3 mt-6">
            <button
              type="button"
              (click)="close.emit()"
              class="px-4 py-2 border border-secondary-300 text-secondary-700 rounded-lg hover:bg-secondary-50 transition-colors"
            >
              Annuler
            </button>
            <button
              type="submit"
              [disabled]="form.invalid"
              class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-secondary-300 disabled:cursor-not-allowed transition-colors"
            >
              {{ category ? 'Enregistrer' : 'Creer' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class CategoryModalComponent implements OnInit {
  @Input() category: Category | null = null;
  @Input() parentCategory: Category | null = null;

  @Output() save = new EventEmitter<{
    name: string;
    slug?: string;
    description?: string;
    imageUrl?: string;
    parentId?: string;
    sortOrder?: number;
  }>();
  @Output() close = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [this.category?.name || '', Validators.required],
      slug: [this.category?.slug || ''],
      description: [this.category?.description || ''],
      imageUrl: [this.category?.imageUrl || ''],
      sortOrder: [this.category?.sortOrder || 0],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const data = {
      ...this.form.value,
      parentId: this.parentCategory?.id,
    };

    // Clean empty strings
    if (!data.slug) delete data.slug;
    if (!data.description) delete data.description;
    if (!data.imageUrl) delete data.imageUrl;
    if (!data.parentId) delete data.parentId;

    this.save.emit(data);
  }
}
