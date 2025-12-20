import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { PageResponse } from '../../../core/models';
import {
  StockItem,
  StockMovement,
  AddStockRequest,
  RemoveStockRequest,
  AdjustStockRequest,
  UpdateStockSettingsRequest,
  Reservation,
  CreateReservationRequest,
} from '../models/inventory.model';

@Injectable({
  providedIn: 'root',
})
export class InventoryService {
  private readonly api = inject(ApiService);

  // ==================== Stock Items ====================

  getStockItems(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PageResponse<StockItem>> {
    return this.api.getPage<StockItem>('/inventory', params);
  }

  getStockByProductId(productId: string): Observable<StockItem> {
    return this.api.get<StockItem>(`/inventory/products/${productId}`);
  }

  getLowStock(): Observable<StockItem[]> {
    return this.api.get<StockItem[]>('/inventory/low-stock');
  }

  getOutOfStock(): Observable<StockItem[]> {
    return this.api.get<StockItem[]>('/inventory/out-of-stock');
  }

  addStock(request: AddStockRequest): Observable<StockItem> {
    return this.api.post<StockItem>('/inventory/add', request);
  }

  removeStock(request: RemoveStockRequest): Observable<StockItem> {
    return this.api.post<StockItem>('/inventory/remove', request);
  }

  adjustStock(request: AdjustStockRequest): Observable<StockItem> {
    return this.api.post<StockItem>('/inventory/adjust', request);
  }

  updateStockSettings(
    productId: string,
    request: UpdateStockSettingsRequest
  ): Observable<StockItem> {
    return this.api.put<StockItem>(`/inventory/products/${productId}/settings`, request);
  }

  // ==================== Stock Movements ====================

  getMovementHistory(productId: string): Observable<StockMovement[]> {
    return this.api.get<StockMovement[]>(`/inventory/products/${productId}/movements`);
  }

  // ==================== Reservations ====================

  getReservationsByProduct(productId: string): Observable<Reservation[]> {
    return this.api.get<Reservation[]>(`/reservations/product/${productId}`);
  }

  createReservation(request: CreateReservationRequest): Observable<Reservation> {
    return this.api.post<Reservation>('/reservations', request);
  }

  confirmReservation(id: string): Observable<Reservation> {
    return this.api.post<Reservation>(`/reservations/${id}/confirm`, {});
  }

  cancelReservation(id: string): Observable<void> {
    return this.api.post<void>(`/reservations/${id}/cancel`, {});
  }

  // ==================== Utilities ====================

  getMovementTypeLabel(type: string): string {
    switch (type) {
      case 'IN':
        return 'Entree';
      case 'OUT':
        return 'Sortie';
      case 'ADJUSTMENT':
        return 'Ajustement';
      case 'RESERVATION':
        return 'Reservation';
      case 'RELEASE':
        return 'Liberation';
      case 'TRANSFER':
        return 'Transfert';
      default:
        return type;
    }
  }

  getMovementTypeClass(type: string): string {
    switch (type) {
      case 'IN':
        return 'bg-green-100 text-green-800';
      case 'OUT':
        return 'bg-red-100 text-red-800';
      case 'ADJUSTMENT':
        return 'bg-blue-100 text-blue-800';
      case 'RESERVATION':
        return 'bg-yellow-100 text-yellow-800';
      case 'RELEASE':
        return 'bg-purple-100 text-purple-800';
      case 'TRANSFER':
        return 'bg-secondary-100 text-secondary-800';
      default:
        return 'bg-secondary-100 text-secondary-800';
    }
  }
}
