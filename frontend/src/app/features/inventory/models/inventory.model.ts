// Stock Item Models
export interface StockItem {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  availableQuantity: number;
  reservedQuantity: number;
  physicalQuantity: number;
  lowStockThreshold: number;
  location?: string;
  lowStock: boolean;
  outOfStock: boolean;
  createdAt: string;
  updatedAt: string;
}

// Movement Types
export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT' | 'RESERVATION' | 'RELEASE' | 'TRANSFER';

// Stock Movement
export interface StockMovement {
  id: string;
  productId: string;
  productName: string;
  type: MovementType;
  quantity: number;
  quantityBefore: number;
  quantityAfter: number;
  reference?: string;
  reservationId?: string;
  reason?: string;
  performedBy?: string;
  createdAt: string;
}

// Request DTOs
export interface AddStockRequest {
  productId: string;
  quantity: number;
  reference?: string;
  reason?: string;
}

export interface RemoveStockRequest {
  productId: string;
  quantity: number;
  reference?: string;
  reason?: string;
}

export interface AdjustStockRequest {
  productId: string;
  newQuantity: number;
  reason?: string;
}

export interface UpdateStockSettingsRequest {
  lowStockThreshold?: number;
  location?: string;
}

// Reservation Models
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Reservation {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  status: ReservationStatus;
  orderId?: string;
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReservationRequest {
  productId: string;
  quantity: number;
  orderId?: string;
  expiresInMinutes?: number;
}
