import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Category } from '../models/catalog.model';

@Component({
  selector: 'app-category-tree-item',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="border-b border-secondary-100 last:border-b-0">
      <div
        class="flex items-center gap-3 py-3 px-2 hover:bg-secondary-50 rounded-lg transition-colors"
        [style.padding-left.px]="level * 24 + 8"
      >
        <!-- Expand/Collapse -->
        @if (hasChildren) {
          <button
            (click)="toggleExpanded()"
            class="w-6 h-6 flex items-center justify-center text-secondary-500 hover:text-secondary-700"
          >
            <span class="text-sm">{{ isExpanded() ? '▼' : '▶' }}</span>
          </button>
        } @else {
          <div class="w-6 h-6"></div>
        }

        <!-- Icon -->
        <div class="w-8 h-8 rounded-lg bg-primary-100 flex items-center justify-center">
          <span class="text-sm">📁</span>
        </div>

        <!-- Name -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <span class="font-medium text-secondary-900 truncate">{{ category.name }}</span>
            @if (!category.active) {
              <span class="px-2 py-0.5 text-xs bg-secondary-100 text-secondary-600 rounded">
                Inactif
              </span>
            }
          </div>
          @if (category.description) {
            <p class="text-sm text-secondary-500 truncate">{{ category.description }}</p>
          }
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-1">
          <button
            (click)="addChild.emit(category)"
            class="p-2 text-secondary-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
            title="Ajouter sous-categorie"
          >
            <span class="text-sm">➕</span>
          </button>
          <button
            (click)="edit.emit(category)"
            class="p-2 text-secondary-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
            title="Modifier"
          >
            <span class="text-sm">✏️</span>
          </button>
          <button
            (click)="toggleStatus.emit(category)"
            class="p-2 text-secondary-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
            [title]="category.active ? 'Desactiver' : 'Activer'"
          >
            <span class="text-sm">{{ category.active ? '🔒' : '🔓' }}</span>
          </button>
          <button
            (click)="delete.emit(category)"
            class="p-2 text-secondary-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            title="Supprimer"
          >
            <span class="text-sm">🗑️</span>
          </button>
        </div>
      </div>

      <!-- Children -->
      @if (isExpanded() && hasChildren) {
        @for (child of category.children; track child.id) {
          <app-category-tree-item
            [category]="child"
            [level]="level + 1"
            (edit)="edit.emit($event)"
            (delete)="delete.emit($event)"
            (toggleStatus)="toggleStatus.emit($event)"
            (addChild)="addChild.emit($event)"
          />
        }
      }
    </div>
  `,
})
export class CategoryTreeItemComponent {
  @Input({ required: true }) category!: Category;
  @Input() level = 0;

  @Output() edit = new EventEmitter<Category>();
  @Output() delete = new EventEmitter<Category>();
  @Output() toggleStatus = new EventEmitter<Category>();
  @Output() addChild = new EventEmitter<Category>();

  isExpanded = signal(true);

  get hasChildren(): boolean {
    return !!this.category.children && this.category.children.length > 0;
  }

  toggleExpanded(): void {
    this.isExpanded.update((v) => !v);
  }
}
