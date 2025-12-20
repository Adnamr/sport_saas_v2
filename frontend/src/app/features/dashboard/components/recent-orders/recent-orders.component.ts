import { Component, Input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RecentOrder } from '../../models/dashboard.model';

const STATUS_CLASSES: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-purple-100 text-purple-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
  DRAFT: 'bg-gray-100 text-gray-800',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'En attente',
  CONFIRMED: 'Confirmee',
  IN_PROGRESS: 'En cours',
  COMPLETED: 'Terminee',
  CANCELLED: 'Annulee',
  DRAFT: 'Brouillon',
};

@Component({
  selector: 'app-recent-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-secondary-900">Commandes recentes</h2>
        <a routerLink="/orders" class="text-sm text-primary-600 hover:text-primary-700 font-medium">
          Voir tout
        </a>
      </div>

      @if (orders.length === 0) {
        <div class="text-center py-8 text-secondary-500">
          Aucune commande recente
        </div>
      } @else {
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="text-left text-xs font-medium text-secondary-500 uppercase tracking-wider">
                <th class="pb-3">Commande</th>
                <th class="pb-3">Client</th>
                <th class="pb-3">Statut</th>
                <th class="pb-3 text-right">Montant</th>
                <th class="pb-3 text-right">Date</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-secondary-100">
              @for (order of orders; track order.id) {
                <tr class="hover:bg-secondary-50">
                  <td class="py-3">
                    <a
                      [routerLink]="['/orders', order.id]"
                      class="text-sm font-medium text-primary-600 hover:text-primary-700"
                    >
                      {{ order.orderNumber }}
                    </a>
                    <p class="text-xs text-secondary-500">{{ order.type }}</p>
                  </td>
                  <td class="py-3">
                    <span class="text-sm text-secondary-900">{{ order.customerName }}</span>
                  </td>
                  <td class="py-3">
                    <span
                      class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      [ngClass]="getStatusClass(order.status)"
                    >
                      {{ getStatusLabel(order.status) }}
                    </span>
                  </td>
                  <td class="py-3 text-right">
                    <span class="text-sm font-medium text-secondary-900">
                      {{ formatCurrency(order.total, order.currency) }}
                    </span>
                  </td>
                  <td class="py-3 text-right">
                    <span class="text-sm text-secondary-500">
                      {{ order.createdAt | date: 'dd/MM/yyyy' }}
                    </span>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
})
export class RecentOrdersComponent {
  @Input() orders: RecentOrder[] = [];

  getStatusClass(status: string): string {
    return STATUS_CLASSES[status] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    return STATUS_LABELS[status] || status;
  }

  formatCurrency(value: number, currency: string): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency || 'EUR',
    }).format(value);
  }
}
