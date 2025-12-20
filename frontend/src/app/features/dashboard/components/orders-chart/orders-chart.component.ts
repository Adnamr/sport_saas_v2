import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ElementRef,
  ViewChild,
  AfterViewInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { OrdersByStatus } from '../../models/dashboard.model';

Chart.register(...registerables);

const STATUS_COLORS: Record<string, string> = {
  PENDING: '#F59E0B',
  CONFIRMED: '#3B82F6',
  IN_PROGRESS: '#8B5CF6',
  COMPLETED: '#10B981',
  CANCELLED: '#EF4444',
  DRAFT: '#9CA3AF',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'En attente',
  CONFIRMED: 'Confirmees',
  IN_PROGRESS: 'En cours',
  COMPLETED: 'Terminees',
  CANCELLED: 'Annulees',
  DRAFT: 'Brouillons',
};

@Component({
  selector: 'app-orders-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-secondary-900">Commandes par statut</h2>
        <p class="text-2xl font-bold text-secondary-900">{{ data?.total || 0 }}</p>
      </div>
      <div class="flex items-center gap-6">
        <div class="w-48 h-48">
          <canvas #chartCanvas></canvas>
        </div>
        <div class="flex-1 space-y-2">
          @for (item of statusItems; track item.status) {
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-3 h-3 rounded-full" [style.background-color]="item.color"></div>
                <span class="text-sm text-secondary-600">{{ item.label }}</span>
              </div>
              <span class="text-sm font-medium text-secondary-900">{{ item.count }}</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class OrdersChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;
  @Input() data?: OrdersByStatus | null;

  private chart?: Chart;
  statusItems: { status: string; label: string; count: number; color: string }[] = [];

  ngAfterViewInit(): void {
    this.createChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.updateStatusItems();
      if (this.chart) {
        this.updateChart();
      }
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private updateStatusItems(): void {
    if (!this.data?.byStatus) {
      this.statusItems = [];
      return;
    }

    this.statusItems = Object.entries(this.data.byStatus).map(([status, count]) => ({
      status,
      label: STATUS_LABELS[status] || status,
      count,
      color: STATUS_COLORS[status] || '#9CA3AF',
    }));
  }

  private createChart(): void {
    if (!this.chartCanvas) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const labels = this.statusItems.map((item) => item.label);
    const data = this.statusItems.map((item) => item.count);
    const colors = this.statusItems.map((item) => item.color);

    this.chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [
          {
            data,
            backgroundColor: colors,
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        cutout: '70%',
        plugins: {
          legend: {
            display: false,
          },
        },
      },
    });
  }

  private updateChart(): void {
    if (!this.chart) return;

    const labels = this.statusItems.map((item) => item.label);
    const data = this.statusItems.map((item) => item.count);
    const colors = this.statusItems.map((item) => item.color);

    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = data;
    this.chart.data.datasets[0].backgroundColor = colors;
    this.chart.update();
  }
}
