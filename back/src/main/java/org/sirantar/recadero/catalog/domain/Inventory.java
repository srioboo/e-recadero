package org.sirantar.recadero.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inventory levels for catalog products and variants.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory", schema = "catalog")
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant productVariant;

  @Column(name = "quantity_available", nullable = false)
  private Integer quantityAvailable = 0;

  @Column(name = "quantity_reserved", nullable = false)
  private Integer quantityReserved = 0;

  @Column(name = "quantity_damaged", nullable = false)
  private Integer quantityDamaged = 0;

  @Column(name = "reorder_level")
  private Integer reorderLevel = 10;

  @Column(name = "warehouse_location", length = 255)
  private String warehouseLocation;

  @Column(name = "last_restock_date")
  private LocalDateTime lastRestockDate;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
