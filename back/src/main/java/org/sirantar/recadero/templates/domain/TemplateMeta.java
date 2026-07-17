package org.sirantar.recadero.templates.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * SEO/social metadata for a template (one row per template).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "template_meta", schema = "templates")
public class TemplateMeta {

  @Id
  private UUID id;

  @Column(name = "template_id", nullable = false, unique = true)
  private UUID templateId;

  @Column(name = "page_title", length = 255)
  private String pageTitle;

  @Column(name = "page_description", length = 1000)
  private String pageDescription;

  @Column(name = "og_title", length = 255)
  private String ogTitle;

  @Column(name = "og_description", length = 1000)
  private String ogDescription;

  @Column(name = "og_image_url", length = 1000)
  private String ogImageUrl;

  @Column(length = 500)
  private String keywords;

  @Column(name = "canonical_url", length = 1000)
  private String canonicalUrl;

  @Column(name = "robots_directive", length = 100)
  private String robotsDirective;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "structured_data_json", columnDefinition = "jsonb")
  private String structuredDataJson;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
