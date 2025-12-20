import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stock-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (outOfStock) {
      <span class="px-2 py-1 text-xs font-medium rounded-full bg-red-100 text-red-800">
        Rupture
      </span>
    } @else if (lowStock) {
      <span class="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800">
        Stock bas
      </span>
    } @else {
      <span class="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
        OK
      </span>
    }
  `,
})
export class StockBadgeComponent {
  @Input() lowStock = false;
  @Input() outOfStock = false;
}
