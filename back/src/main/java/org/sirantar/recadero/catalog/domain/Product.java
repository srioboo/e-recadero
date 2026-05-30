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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product catalog entry.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product", schema = "catalog")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "short_description", length = 500)
  private String shortDescription;

  @Column(nullable = false, unique = true, length = 100)
  private String sku;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "cost_price", precision = 10, scale = 2)
  private BigDecimal costPrice;

  @Column(name = "is_active", nullable = false)
  private Boolean active = Boolean.TRUE;

  @Column(name = "is_featured", nullable = false)
  private Boolean featured = Boolean.FALSE;

  @Column(precision = 3, scale = 2)
  private BigDecimal rating;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "updated_by", length = 255)
  private String updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  private Long version;
}
