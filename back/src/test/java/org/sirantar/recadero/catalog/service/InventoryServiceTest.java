package org.sirantar.recadero.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.catalog.domain.Inventory;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.InventoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.dto.InventoryResponse;
import org.sirantar.recadero.catalog.service.dto.ProductAvailabilityResponse;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private InventoryRepository inventoryRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private InventoryService inventoryService;

  private Inventory testInventory;
  private ProductVariant testVariant;
  private UUID warehouseId;

  @BeforeEach
  void setUp() {
    warehouseId = UUID.nameUUIDFromBytes("DEFAULT_WAREHOUSE".getBytes(StandardCharsets.UTF_8));

    testVariant = new ProductVariant();
    testVariant.setId(1L);
    testVariant.setSku("VAR-001");

    testInventory = new Inventory();
    testInventory.setId(1L);
    testInventory.setProductVariant(testVariant);
    testInventory.setWarehouseId(warehouseId);
    testInventory.setQuantityAvailable(100);
    testInventory.setQuantityReserved(10);
    testInventory.setQuantityDamaged(0);
    testInventory.setReorderLevel(5);
  }

  @Test
  void testCheckAvailabilitySuccess() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));

    int available = inventoryService.checkAvailability(1L, 50);

    assertThat(available).isEqualTo(100);
  }

  @Test
  void testCheckAvailabilityNotFound() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.empty());

    int available = inventoryService.checkAvailability(1L, 50);

    assertThat(available).isEqualTo(0);
  }

  @Test
  void testReserveInventorySuccess() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    int remaining = inventoryService.reserveInventory(1L, 20);

    assertThat(testInventory.getQuantityAvailable()).isEqualTo(80);
    assertThat(testInventory.getQuantityReserved()).isEqualTo(30);
  }

  @Test
  void testReserveInventoryInsufficientStock() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));

    assertThatThrownBy(() -> inventoryService.reserveInventory(1L, 150))
        .isInstanceOf(OutOfStockException.class);
  }

  @Test
  void testReleaseReservationSuccess() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    int remaining = inventoryService.releaseReservation(1L, 10);

    assertThat(testInventory.getQuantityAvailable()).isEqualTo(110);
    assertThat(testInventory.getQuantityReserved()).isEqualTo(0);
  }

  @Test
  void testReleaseReservationMoreThanReserved() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    inventoryService.releaseReservation(1L, 50);

    assertThat(testInventory.getQuantityReserved()).isEqualTo(0);
  }

  @Test
  void testAdjustInventoryPositiveChange() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    int result = inventoryService.adjustInventory(1L, 50, "RESTOCK");

    assertThat(testInventory.getQuantityAvailable()).isEqualTo(150);
  }

  @Test
  void testAdjustInventoryNegativeChange() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    int result = inventoryService.adjustInventory(1L, -30, "LOSS");

    assertThat(testInventory.getQuantityAvailable()).isEqualTo(70);
  }

  @Test
  void testAdjustInventoryWouldGoNegative() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));

    assertThatThrownBy(() -> inventoryService.adjustInventory(1L, -150, "LOSS"))
        .isInstanceOf(OutOfStockException.class);
  }

  @Test
  void testGetInventorySuccess() {
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));

    InventoryResponse response = inventoryService.getInventory(1L);

    assertThat(response).isNotNull();
    assertThat(response.productVariantId()).isEqualTo(1L);
  }

  @Test
  void testListLowStockSuccess() {
    when(inventoryRepository.findLowStock()).thenReturn(List.of(testInventory));

    List<InventoryResponse> result = inventoryService.listLowStock();

    assertThat(result).hasSize(1);
  }

  @Test
  void testListLowStockEmpty() {
    when(inventoryRepository.findLowStock()).thenReturn(List.of());

    List<InventoryResponse> result = inventoryService.listLowStock();

    assertThat(result).isEmpty();
  }

  @Test
  void testGetProductAvailabilitySuccess() {
    Product product = new Product();
    product.setId(1L);

    Inventory inv1 = new Inventory();
    inv1.setId(1L);
    inv1.setProductVariant(testVariant);
    inv1.setQuantityAvailable(50);

    when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(testVariant));
    when(inventoryRepository.findByProductVariant_Id(1L)).thenReturn(List.of(inv1));
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    ProductAvailabilityResponse response = inventoryService.getProductAvailability(1L);

    assertThat(response).isNotNull();
    assertThat(response.productId()).isEqualTo(1L);
  }

  @Test
  void testGetProductAvailabilityNotFound() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.getProductAvailability(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void testInventoryEdgeCaseZeroAvailable() {
    testInventory.setQuantityAvailable(0);

    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));

    assertThatThrownBy(() -> inventoryService.reserveInventory(1L, 1))
        .isInstanceOf(OutOfStockException.class);
  }

  @Test
  void testInventoryEdgeCaseExactAvailable() {
    testInventory.setQuantityAvailable(50);

    when(inventoryRepository.findByProductVariantIdAndWarehouseId(1L, warehouseId))
        .thenReturn(Optional.of(testInventory));
    when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

    int result = inventoryService.reserveInventory(1L, 50);

    assertThat(testInventory.getQuantityAvailable()).isEqualTo(0);
  }
}
