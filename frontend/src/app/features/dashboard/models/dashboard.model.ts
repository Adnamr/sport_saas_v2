export interface DashboardStats {
  totalRevenue: number;
  monthlyRevenue: number;
  revenueGrowth: number;
  totalOrders: number;
  monthlyOrders: number;
  pendingOrders: number;
  totalProducts: number;
  lowStockProducts: number;
  outOfStockProducts: number;
  totalCustomers: number;
  newCustomersThisMonth: number;
  activeRentals: number;
}

export interface RevenueChart {
  period: string;
  labels: string[];
  revenues: number[];
  total: number;
  average: number;
}

export interface OrdersByStatus {
  total: number;
  byStatus: Record<string, number>;
}

export interface RecentOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  status: string;
  type: string;
  total: number;
  currency: string;
  createdAt: string;
}

export interface LowStockItem {
  productId: string;
  productName: string;
  productSku: string;
  availableQuantity: number;
  lowStockThreshold: number;
  location: string;
  isOutOfStock: boolean;
}

export interface Activity {
  id: string;
  type: string;
  description: string;
  userId: string;
  userName: string;
  entityType: string;
  entityId: string;
  createdAt: string;
}
