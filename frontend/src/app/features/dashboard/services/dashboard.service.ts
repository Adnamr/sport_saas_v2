import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { PageResponse } from '../../../core/models';
import {
  DashboardStats,
  RevenueChart,
  OrdersByStatus,
  RecentOrder,
  LowStockItem,
  Activity,
} from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly api = inject(ApiService);

  getStats(): Observable<DashboardStats> {
    return this.api.get<DashboardStats>('/dashboard/stats');
  }

  getRevenueChart(period: string = 'last_12_months'): Observable<RevenueChart> {
    return this.api.get<RevenueChart>('/dashboard/revenue-chart', { period });
  }

  getOrdersByStatus(): Observable<OrdersByStatus> {
    return this.api.get<OrdersByStatus>('/dashboard/orders-by-status');
  }

  getRecentOrders(limit: number = 10): Observable<RecentOrder[]> {
    return this.api.get<RecentOrder[]>('/dashboard/recent-orders', { limit });
  }

  getLowStockItems(limit: number = 20): Observable<LowStockItem[]> {
    return this.api.get<LowStockItem[]>('/dashboard/low-stock', { limit });
  }

  getRecentActivities(page: number = 0, size: number = 10): Observable<PageResponse<Activity>> {
    return this.api.getPage<Activity>('/dashboard/activity', { page, size });
  }
}
