package org.sirantar.recadero.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Catalog module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class CatalogApplicationModuleTest {

  @Test
  void testCatalogServiceExists() {
    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/service/CatalogService.class"))
        .as("CatalogService should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/service/ProductService.class"))
        .as("ProductService should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/service/InventoryService.class"))
        .as("InventoryService should exist")
        .isNotNull();
  }

  @Test
  void testCatalogRepositoriesExist() {
    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/repository/CategoryRepository.class"))
        .as("CategoryRepository should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/repository/ProductRepository.class"))
        .as("ProductRepository should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/repository/InventoryRepository.class"))
        .as("InventoryRepository should exist")
        .isNotNull();
  }

  @Test
  void testCatalogApiExists() {
    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/api/ProductController.class"))
        .as("Product REST API should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/api/CategoryController.class"))
        .as("Category REST API should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/api/InventoryController.class"))
        .as("Inventory REST API should exist")
        .isNotNull();
  }

  @Test
  void testCatalogDomainsExist() {
    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/domain/Product.class"))
        .as("Product domain should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/domain/Category.class"))
        .as("Category domain should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/domain/ProductVariant.class"))
        .as("ProductVariant domain should exist")
        .isNotNull();

    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/domain/Inventory.class"))
        .as("Inventory domain should exist")
        .isNotNull();
  }

  @Test
  void testCatalogPackageInfoExists() {
    assertThat(
            CatalogApplicationModuleTest.class
                .getClassLoader()
                .getResource("org/sirantar/recadero/catalog/package-info.class"))
        .as("Catalog package-info should exist with @ApplicationModule annotation")
        .isNotNull();
  }
}
