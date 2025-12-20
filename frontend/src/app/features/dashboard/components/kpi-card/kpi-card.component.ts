import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6 hover:shadow-md transition-shadow">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-secondary-600">{{ title }}</p>
          <p class="text-2xl font-bold text-secondary-900 mt-1">{{ displayValue }}</p>
          @if (subtitle) {
            <p class="text-xs text-secondary-500 mt-1">{{ subtitle }}</p>
          }
          @if (growth !== null && growth !== undefined) {
            <div class="flex items-center mt-2">
              <span
                class="text-sm font-medium"
                [class.text-green-600]="growth >= 0"
                [class.text-red-600]="growth < 0"
              >
                {{ growth >= 0 ? '+' : '' }}{{ growth | number: '1.1-1' }}%
              </span>
              <span class="text-xs text-secondary-500 ml-1">vs mois dernier</span>
            </div>
          }
        </div>
        <div
          class="w-12 h-12 rounded-lg flex items-center justify-center"
          [ngClass]="iconBgClass"
        >
          <span class="text-2xl">{{ icon }}</span>
        </div>
      </div>
    </div>
  `,
})
export class KpiCardComponent {
  @Input() title = '';
  @Input() value: number | string = 0;
  @Input() subtitle?: string;
  @Input() growth?: number;
  @Input() icon = '';
  @Input() iconBgClass = 'bg-primary-100';
  @Input() format: 'number' | 'currency' | 'none' = 'number';
  @Input() currency = 'EUR';

  get displayValue(): string {
    if (this.format === 'none' || typeof this.value === 'string') {
      return String(this.value);
    }
    if (this.format === 'currency') {
      return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: this.currency,
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.value as number);
    }
    return new Intl.NumberFormat('fr-FR').format(this.value as number);
  }
}
