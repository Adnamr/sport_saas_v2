import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Activity } from '../../models/dashboard.model';

const ACTIVITY_ICONS: Record<string, string> = {
  ORDER_CREATED: '🛒',
  ORDER_UPDATED: '📝',
  ORDER_COMPLETED: '✅',
  ORDER_CANCELLED: '❌',
  PAYMENT_RECEIVED: '💰',
  PRODUCT_CREATED: '📦',
  PRODUCT_UPDATED: '🔄',
  USER_CREATED: '👤',
  USER_UPDATED: '👥',
  STOCK_UPDATED: '📊',
  RENTAL_STARTED: '🏃',
  RENTAL_ENDED: '🏁',
};

@Component({
  selector: 'app-activity-feed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
      <h2 class="text-lg font-semibold text-secondary-900 mb-4">Activite recente</h2>

      @if (activities.length === 0) {
        <div class="text-center py-8 text-secondary-500">
          Aucune activite recente
        </div>
      } @else {
        <div class="space-y-4">
          @for (activity of activities; track activity.id) {
            <div class="flex items-start gap-3">
              <div class="w-8 h-8 rounded-full bg-secondary-100 flex items-center justify-center flex-shrink-0">
                <span class="text-sm">{{ getIcon(activity.type) }}</span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-secondary-900">{{ activity.description }}</p>
                <div class="flex items-center gap-2 mt-1">
                  <span class="text-xs text-secondary-500">{{ activity.userName }}</span>
                  <span class="text-xs text-secondary-400">•</span>
                  <span class="text-xs text-secondary-500">{{ formatDate(activity.createdAt) }}</span>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class ActivityFeedComponent {
  @Input() activities: Activity[] = [];

  getIcon(type: string): string {
    return ACTIVITY_ICONS[type] || '📌';
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return "A l'instant";
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;
    return date.toLocaleDateString('fr-FR');
  }
}
