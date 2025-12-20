import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from './services/inventory.service';
import { StockItem, StockMovement } from './models/inventory.model';
import { StockBadgeComponent } from './components/stock-badge.component';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, FormsModule, StockBadgeComponent],
  templateUrl: './inventory-list.component.html',
})
export class InventoryListComponent implements OnInit {
  readonly inventoryService = inject(InventoryService);

  stock = signal<StockItem[]>([]);
  movements = signal<StockMovement[]>([]);
  isLoading = signal(true);
  isLoadingMovements = signal(false);
  isSubmitting = signal(false);
  error = signal<string | null>(null);
  actionError = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 20;

  searchQuery = '';
  selectedFilter = 'all';

  // Modals
  showAddStockModal = signal(false);
  showRemoveStockModal = signal(false);
  showAdjustStockModal = signal(false);
  showMovementsModal = signal(false);

  selectedItem = signal<StockItem | null>(null);

  // Modal form fields
  modalProductId = '';
  modalQuantity = 1;
  modalNewQuantity = 0;
  modalReference = '';
  modalReason = '';

  filteredStock = computed(() => {
    let items = this.stock();

    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      items = items.filter(
        (item) =>
          item.productName.toLowerCase().includes(query) ||
          item.productSku.toLowerCase().includes(query)
      );
    }

    return items;
  });

  ngOnInit(): void {
    this.loadStock();
  }

  loadStock(): void {
    this.isLoading.set(true);
    this.error.set(null);

    if (this.selectedFilter === 'low') {
      this.inventoryService.getLowStock().subscribe({
        next: (items) => {
          this.stock.set(items);
          this.totalPages.set(1);
          this.totalElements.set(items.length);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Error loading low stock:', err);
          this.error.set('Erreur lors du chargement des stocks bas');
          this.isLoading.set(false);
        },
      });
    } else if (this.selectedFilter === 'out') {
      this.inventoryService.getOutOfStock().subscribe({
        next: (items) => {
          this.stock.set(items);
          this.totalPages.set(1);
          this.totalElements.set(items.length);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Error loading out of stock:', err);
          this.error.set('Erreur lors du chargement des ruptures');
          this.isLoading.set(false);
        },
      });
    } else {
      this.inventoryService
        .getStockItems({
          page: this.currentPage(),
          size: this.pageSize,
        })
        .subscribe({
          next: (response) => {
            this.stock.set(response.content);
            this.totalPages.set(response.totalPages);
            this.totalElements.set(response.totalElements);
            this.isLoading.set(false);
          },
          error: (err) => {
            console.error('Error loading stock:', err);
            this.error.set('Erreur lors du chargement du stock');
            this.isLoading.set(false);
          },
        });
    }
  }

  onSearchChange(): void {
    // Search is done client-side via computed signal - no action needed
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadStock();
    }
  }

  openAddStock(item?: StockItem): void {
    this.resetModalFields();
    this.selectedItem.set(item || null);
    if (item) {
      this.modalProductId = item.productId;
    }
    this.showAddStockModal.set(true);
  }

  openRemoveStock(item?: StockItem): void {
    this.resetModalFields();
    this.selectedItem.set(item || null);
    if (item) {
      this.modalProductId = item.productId;
    }
    this.showRemoveStockModal.set(true);
  }

  openAdjustStock(item: StockItem): void {
    this.resetModalFields();
    this.selectedItem.set(item);
    this.modalProductId = item.productId;
    this.modalNewQuantity = item.physicalQuantity;
    this.showAdjustStockModal.set(true);
  }

  viewMovements(item: StockItem): void {
    this.selectedItem.set(item);
    this.movements.set([]);
    this.showMovementsModal.set(true);
    this.isLoadingMovements.set(true);

    this.inventoryService.getMovementHistory(item.productId).subscribe({
      next: (movements) => {
        this.movements.set(movements);
        this.isLoadingMovements.set(false);
      },
      error: (err) => {
        console.error('Error loading movements:', err);
        this.isLoadingMovements.set(false);
      },
    });
  }

  closeModals(): void {
    this.showAddStockModal.set(false);
    this.showRemoveStockModal.set(false);
    this.showAdjustStockModal.set(false);
    this.showMovementsModal.set(false);
    this.selectedItem.set(null);
    this.resetModalFields();
  }

  private resetModalFields(): void {
    this.modalProductId = '';
    this.modalQuantity = 1;
    this.modalNewQuantity = 0;
    this.modalReference = '';
    this.modalReason = '';
  }

  addStock(): void {
    const productId = this.selectedItem()?.productId || this.modalProductId;
    if (!productId || this.modalQuantity < 1) return;

    this.isSubmitting.set(true);
    this.actionError.set(null);

    this.inventoryService
      .addStock({
        productId,
        quantity: this.modalQuantity,
        reference: this.modalReference || undefined,
        reason: this.modalReason || undefined,
      })
      .subscribe({
        next: () => {
          this.successMessage.set('Stock ajoute avec succes');
          this.closeModals();
          this.loadStock();
          this.isSubmitting.set(false);
        },
        error: (err) => {
          console.error('Error adding stock:', err);
          this.actionError.set("Erreur lors de l'ajout du stock");
          this.isSubmitting.set(false);
        },
      });
  }

  removeStock(): void {
    const productId = this.selectedItem()?.productId || this.modalProductId;
    if (!productId || this.modalQuantity < 1) return;

    this.isSubmitting.set(true);
    this.actionError.set(null);

    this.inventoryService
      .removeStock({
        productId,
        quantity: this.modalQuantity,
        reference: this.modalReference || undefined,
        reason: this.modalReason || undefined,
      })
      .subscribe({
        next: () => {
          this.successMessage.set('Stock retire avec succes');
          this.closeModals();
          this.loadStock();
          this.isSubmitting.set(false);
        },
        error: (err) => {
          console.error('Error removing stock:', err);
          this.actionError.set('Erreur lors du retrait du stock');
          this.isSubmitting.set(false);
        },
      });
  }

  adjustStock(): void {
    const productId = this.selectedItem()?.productId;
    if (!productId || this.modalNewQuantity < 0 || !this.modalReason) return;

    this.isSubmitting.set(true);
    this.actionError.set(null);

    this.inventoryService
      .adjustStock({
        productId,
        newQuantity: this.modalNewQuantity,
        reason: this.modalReason,
      })
      .subscribe({
        next: () => {
          this.successMessage.set('Stock ajuste avec succes');
          this.closeModals();
          this.loadStock();
          this.isSubmitting.set(false);
        },
        error: (err) => {
          console.error('Error adjusting stock:', err);
          this.actionError.set("Erreur lors de l'ajustement du stock");
          this.isSubmitting.set(false);
        },
      });
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
