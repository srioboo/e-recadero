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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product variant with SKU and attribute-specific pricing.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_variant", schema = "catalog")
public class ProductVariant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "variant_sku", nullable = false, unique = true, length = 100)
  private String sku;

  @Column(name = "variant_attributes", columnDefinition = "jsonb")
  private String variantAttributes;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(precision = 8, scale = 3)
  private BigDecimal weight;

  @Version
  private Long version;
}
