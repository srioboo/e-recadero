package org.sirantar.recadero.catalog.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.catalog.domain.Inventory;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.InventoryRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory availability and reservation management.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

  static final UUID DEFAULT_WAREHOUSE_ID =
      UUID.nameUUIDFromBytes("DEFAULT_WAREHOUSE".getBytes(StandardCharsets.UTF_8));

  private final InventoryRepository inventoryRepository;
  private final ProductVariantRepository productVariantRepository;

  @Transactional(readOnly = true)
  public int checkAvailability(Long variantId, int quantity) {
    return findInventory(variantId).map(Inventory::getQuantityAvailable).orElse(0);
  }

  public int reserveInventory(Long variantId, int quantity) {
    Inventory inventory = getOrCreateInventory(variantId);
    if (inventory.getQuantityAvailable() < quantity) {
      throw new OutOfStockException("Insufficient inventory for variant " + variantId);
    }
    inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
    inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
    inventoryRepository.save(inventory);
    return inventory.getQuantityAvailable();
  }

  public int releaseReservation(Long variantId, int quantity) {
    Inventory inventory = getOrCreateInventory(variantId);
    inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));
    inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
    inventoryRepository.save(inventory);
    return inventory.getQuantityAvailable();
  }

  public int adjustInventory(Long variantId, int change, String reason) {
    Inventory inventory = getOrCreateInventory(variantId);
    int newAvailable = inventory.getQuantityAvailable() + change;
    if (newAvailable < 0) {
      throw new OutOfStockException("Inventory adjustment would go negative for variant " + variantId);
    }
    inventory.setQuantityAvailable(newAvailable);
    inventoryRepository.save(inventory);
    return inventory.getQuantityAvailable();
  }

  private java.util.Optional<Inventory> findInventory(Long variantId) {
    return inventoryRepository.findByProductVariantIdAndWarehouseId(variantId, DEFAULT_WAREHOUSE_ID);
  }

  private Inventory getOrCreateInventory(Long variantId) {
    return findInventory(variantId).orElseGet(() -> {
      ProductVariant productVariant = productVariantRepository.findById(variantId)
          .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
              "Product variant not found: " + variantId));
      Inventory inventory = new Inventory();
      inventory.setProductVariant(productVariant);
      inventory.setProduct(productVariant.getProduct());
      inventory.setWarehouseId(DEFAULT_WAREHOUSE_ID);
      inventory.setQuantityAvailable(0);
      inventory.setQuantityReserved(0);
      return inventory;
    });
  }
}
