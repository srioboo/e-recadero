package org.sirantar.recadero.catalog.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.catalog.service.dto.InventoryAdjustRequest;
import org.sirantar.recadero.catalog.service.dto.InventoryResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inventory REST API.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  @GetMapping("/{variantId}")
  public InventoryResponse getInventory(@PathVariable Long variantId) {
    return inventoryService.getInventory(variantId);
  }

  @PostMapping("/{variantId}/adjust")
  @AdminOnly
  public InventoryResponse adjustInventory(
      @PathVariable Long variantId, @RequestBody InventoryAdjustRequest request) {
    inventoryService.adjustInventory(variantId, request.quantityChange(), request.reason());
    return inventoryService.getInventory(variantId);
  }

  @GetMapping("/low-stock")
  @AdminOnly
  public List<InventoryResponse> lowStock() {
    return inventoryService.listLowStock();
  }
}
