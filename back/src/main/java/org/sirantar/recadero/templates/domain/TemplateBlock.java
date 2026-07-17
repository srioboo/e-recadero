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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single ordered content block within a template. {@link #contentJson} holds
 * the raw JSON matching one of the shapes documented per {@link BlockType} in
 * templates-contract.md; it is parsed/serialized by the service layer rather
 * than mapped to a Java type, since each block type has a different shape.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "template_block", schema = "templates")
public class TemplateBlock {

  @Id
  private UUID id;

  @Column(name = "template_id", nullable = false)
  private UUID templateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "block_type", nullable = false, length = 30)
  private BlockType blockType;

  @Column(name = "block_name", length = 255)
  private String blockName;

  @Column(name = "block_order", nullable = false)
  private Integer blockOrder;

  @Column(name = "is_visible", nullable = false)
  private Boolean isVisible = Boolean.TRUE;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
  private String contentJson = "{}";

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
