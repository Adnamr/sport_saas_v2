package com.sportsaas.inventory.api;

import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.api.dto.*;
import com.sportsaas.inventory.domain.StockItem;
import com.sportsaas.inventory.domain.StockMovement;
import com.sportsaas.inventory.domain.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Gestion du stock")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryController {

    private final StockService stockService;
    private final InventoryMapper inventoryMapper;

    @GetMapping
    @Operation(summary = "Lister tous les stocks")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<StockItemResponse>> list(Pageable pageable) {
        Page<StockItem> page = stockService.findAll(pageable);
        return ResponseEntity.ok(page.map(inventoryMapper::toStockItemResponse));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Recuperer le stock d'un produit")
    public ResponseEntity<StockItemResponse> getStock(@PathVariable UUID productId) {
        StockItem stockItem = stockService.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock for product", productId));
        return ResponseEntity.ok(inventoryMapper.toStockItemResponse(stockItem));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Lister les produits en stock bas")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<StockItemResponse>> getLowStock() {
        List<StockItem> items = stockService.findLowStock();
        return ResponseEntity.ok(inventoryMapper.toStockItemResponseList(items));
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Lister les produits en rupture")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<StockItemResponse>> getOutOfStock() {
        List<StockItem> items = stockService.findOutOfStock();
        return ResponseEntity.ok(inventoryMapper.toStockItemResponseList(items));
    }

    @PostMapping("/add")
    @Operation(summary = "Ajouter du stock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Transactional
    public ResponseEntity<StockItemResponse> addStock(@Valid @RequestBody AddStockRequest request) {
        StockItem stockItem = stockService.addStock(
                request.getProductId(),
                request.getQuantity(),
                request.getReference(),
                request.getReason()
        );
        return ResponseEntity.ok(inventoryMapper.toStockItemResponse(stockItem));
    }

    @PostMapping("/remove")
    @Operation(summary = "Retirer du stock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Transactional
    public ResponseEntity<StockItemResponse> removeStock(@Valid @RequestBody RemoveStockRequest request) {
        StockItem stockItem = stockService.removeStock(
                request.getProductId(),
                request.getQuantity(),
                request.getReference(),
                request.getReason()
        );
        return ResponseEntity.ok(inventoryMapper.toStockItemResponse(stockItem));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Ajuster le stock (inventaire)")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Transactional
    public ResponseEntity<StockItemResponse> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        StockItem stockItem = stockService.adjustStock(
                request.getProductId(),
                request.getNewQuantity(),
                request.getReason()
        );
        return ResponseEntity.ok(inventoryMapper.toStockItemResponse(stockItem));
    }

    @PutMapping("/products/{productId}/settings")
    @Operation(summary = "Mettre a jour les parametres de stock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Transactional
    public ResponseEntity<StockItemResponse> updateSettings(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateStockSettingsRequest request) {

        StockItem stockItem = stockService.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock for product", productId));

        if (request.getLowStockThreshold() != null) {
            stockItem = stockService.updateLowStockThreshold(productId, request.getLowStockThreshold());
        }

        if (request.getLocation() != null) {
            stockItem = stockService.updateLocation(productId, request.getLocation());
        }

        return ResponseEntity.ok(inventoryMapper.toStockItemResponse(stockItem));
    }

    @GetMapping("/products/{productId}/movements")
    @Operation(summary = "Historique des mouvements de stock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<StockMovementResponse>> getMovements(@PathVariable UUID productId) {
        List<StockMovement> movements = stockService.getMovementHistory(productId);
        return ResponseEntity.ok(inventoryMapper.toStockMovementResponseList(movements));
    }
}
