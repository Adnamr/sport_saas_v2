import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LowStockItem } from '../../models/dashboard.model';

@Component({
  selector: 'app-low-stock',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-secondary-900">Stock bas</h2>
        <a routerLink="/inventory" class="text-sm text-primary-600 hover:text-primary-700 font-medium">
          Voir tout
        </a>
      </div>

      @if (items.length === 0) {
        <div class="text-center py-8 text-secondary-500">
          Aucun produit en stock bas
        </div>
      } @else {
        <div class="space-y-3">
          @for (item of items; track item.productId) {
            <div
              class="flex items-center justify-between p-3 rounded-lg"
              [ngClass]="item.isOutOfStock ? 'bg-red-50' : 'bg-yellow-50'"
            >
              <div class="flex-1 min-w-0">
                <a
                  [routerLink]="['/catalog', item.productId]"
                  class="text-sm font-medium text-secondary-900 hover:text-primary-600 truncate block"
                >
                  {{ item.productName }}
                </a>
                <p class="text-xs text-secondary-500">{{ item.productSku }}</p>
              </div>
              <div class="ml-4 text-right">
                <p
                  class="text-sm font-bold"
                  [ngClass]="item.isOutOfStock ? 'text-red-600' : 'text-yellow-600'"
                >
                  {{ item.availableQuantity }} / {{ item.lowStockThreshold }}
                </p>
                <p class="text-xs text-secondary-500">{{ item.location || 'N/A' }}</p>
              </div>
              <div class="ml-3">
                @if (item.isOutOfStock) {
                  <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                    Rupture
                  </span>
                } @else {
                  <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                    Bas
                  </span>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class LowStockComponent {
  @Input() items: LowStockItem[] = [];
}
