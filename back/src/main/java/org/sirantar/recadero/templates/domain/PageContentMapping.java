package org.sirantar.recadero.templates.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Which template renders a given product/category/landing-page/user entity.
 * {@link #entityId} is stored as a string since it may originate from
 * different modules with different ID formats (e.g. catalog's Long ids).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "page_content_mapping", schema = "templates")
public class PageContentMapping {

  @Id
  private UUID id;

  @Column(name = "template_id", nullable = false)
  private UUID templateId;

  @Column(name = "entity_id", nullable = false, length = 255)
  private String entityId;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 30)
  private EntityType entityType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MappingStatus status = MappingStatus.PUBLISHED;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;
}
