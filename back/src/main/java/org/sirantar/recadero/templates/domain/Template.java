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
 * A page template: an ordered set of content blocks plus SEO metadata,
 * versioned on each publish.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "template", schema = "templates")
public class Template {

  @Id
  private UUID id;

  @Column(nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TemplateType type;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TemplateStatus status = TemplateStatus.DRAFT;

  @Column(nullable = false)
  private Integer version = 1;

  @Column(name = "published_version")
  private Integer publishedVersion;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;
}
