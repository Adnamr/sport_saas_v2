import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, catchError, of } from 'rxjs';
import { DashboardService } from './services/dashboard.service';
import { KpiCardComponent } from './components/kpi-card/kpi-card.component';
import { RevenueChartComponent } from './components/revenue-chart/revenue-chart.component';
import { OrdersChartComponent } from './components/orders-chart/orders-chart.component';
import { RecentOrdersComponent } from './components/recent-orders/recent-orders.component';
import { LowStockComponent } from './components/low-stock/low-stock.component';
import { ActivityFeedComponent } from './components/activity-feed/activity-feed.component';
import { QuickActionsComponent } from './components/quick-actions/quick-actions.component';
import {
  DashboardStats,
  RevenueChart,
  OrdersByStatus,
  RecentOrder,
  LowStockItem,
  Activity,
} from './models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    KpiCardComponent,
    RevenueChartComponent,
    OrdersChartComponent,
    RecentOrdersComponent,
    LowStockComponent,
    ActivityFeedComponent,
    QuickActionsComponent,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  stats = signal<DashboardStats | null>(null);
  revenueChart = signal<RevenueChart | null>(null);
  ordersByStatus = signal<OrdersByStatus | null>(null);
  recentOrders = signal<RecentOrder[]>([]);
  lowStockItems = signal<LowStockItem[]>([]);
  activities = signal<Activity[]>([]);

  isLoading = signal(true);
  error = signal<string | null>(null);

  // Computed values for safe subtitle display
  pendingOrdersText = computed(() => {
    const pending = this.stats()?.pendingOrders;
    return pending !== undefined ? `${pending} en attente` : '';
  });

  newCustomersText = computed(() => {
    const newCustomers = this.stats()?.newCustomersThisMonth;
    return newCustomers !== undefined ? `+${newCustomers} ce mois` : '';
  });

  lowStockText = computed(() => {
    const lowStock = this.stats()?.lowStockProducts;
    return lowStock !== undefined ? `${lowStock} en stock bas` : '';
  });

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading.set(true);
    this.error.set(null);

    forkJoin({
      stats: this.dashboardService.getStats().pipe(catchError(() => of(null))),
      revenueChart: this.dashboardService.getRevenueChart().pipe(catchError(() => of(null))),
      ordersByStatus: this.dashboardService.getOrdersByStatus().pipe(catchError(() => of(null))),
      recentOrders: this.dashboardService.getRecentOrders(5).pipe(catchError(() => of([]))),
      lowStockItems: this.dashboardService.getLowStockItems(5).pipe(catchError(() => of([]))),
      activities: this.dashboardService.getRecentActivities(0, 5).pipe(catchError(() => of({ content: [] }))),
    }).subscribe({
      next: (data) => {
        this.stats.set(data.stats);
        this.revenueChart.set(data.revenueChart);
        this.ordersByStatus.set(data.ordersByStatus);
        this.recentOrders.set(data.recentOrders);
        this.lowStockItems.set(data.lowStockItems);
        this.activities.set(data.activities.content || []);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.error.set('Erreur lors du chargement du dashboard');
        this.isLoading.set(false);
      },
    });
  }

  retry(): void {
    this.loadDashboardData();
  }
}
