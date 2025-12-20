import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
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

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading.set(true);
    this.error.set(null);

    // Load stats
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Error loading stats:', err),
    });

    // Load revenue chart
    this.dashboardService.getRevenueChart().subscribe({
      next: (data) => this.revenueChart.set(data),
      error: (err) => console.error('Error loading revenue chart:', err),
    });

    // Load orders by status
    this.dashboardService.getOrdersByStatus().subscribe({
      next: (data) => this.ordersByStatus.set(data),
      error: (err) => console.error('Error loading orders by status:', err),
    });

    // Load recent orders
    this.dashboardService.getRecentOrders(5).subscribe({
      next: (data) => this.recentOrders.set(data),
      error: (err) => console.error('Error loading recent orders:', err),
    });

    // Load low stock items
    this.dashboardService.getLowStockItems(5).subscribe({
      next: (data) => this.lowStockItems.set(data),
      error: (err) => console.error('Error loading low stock items:', err),
    });

    // Load activities
    this.dashboardService.getRecentActivities(0, 5).subscribe({
      next: (data) => {
        this.activities.set(data.content);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading activities:', err);
        this.isLoading.set(false);
      },
    });
  }
}
