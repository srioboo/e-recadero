package org.sirantar.recadero.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dynamic product attribute definition.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "product_attribute",
    schema = "catalog",
    uniqueConstraints = {
      @jakarta.persistence.UniqueConstraint(columnNames = {"product_id", "name"})
    })
public class ProductAttribute {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProductAttributeType type = ProductAttributeType.TEXT;

  @Column(name = "attribute_values", columnDefinition = "jsonb")
  private String values;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;
}
