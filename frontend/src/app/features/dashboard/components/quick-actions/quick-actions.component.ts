import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface QuickAction {
  label: string;
  icon: string;
  route: string;
  color: string;
}

@Component({
  selector: 'app-quick-actions',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
      <h2 class="text-lg font-semibold text-secondary-900 mb-4">Actions rapides</h2>
      <div class="grid grid-cols-2 gap-3">
        @for (action of actions; track action.label) {
          <a
            [routerLink]="action.route"
            class="flex items-center gap-3 p-3 rounded-lg border border-secondary-200 hover:border-primary-300 hover:bg-primary-50 transition-colors"
          >
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center"
              [ngClass]="action.color"
            >
              <span class="text-lg">{{ action.icon }}</span>
            </div>
            <span class="text-sm font-medium text-secondary-900">{{ action.label }}</span>
          </a>
        }
      </div>
    </div>
  `,
})
export class QuickActionsComponent {
  actions: QuickAction[] = [
    {
      label: 'Nouvelle commande',
      icon: '🛒',
      route: '/orders/new',
      color: 'bg-blue-100',
    },
    {
      label: 'Nouveau produit',
      icon: '📦',
      route: '/catalog/new',
      color: 'bg-green-100',
    },
    {
      label: 'Nouvelle location',
      icon: '🏃',
      route: '/rentals/new',
      color: 'bg-purple-100',
    },
    {
      label: 'Nouveau client',
      icon: '👤',
      route: '/customers/new',
      color: 'bg-orange-100',
    },
  ];
}
