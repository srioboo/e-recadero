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
 * Immutable snapshot of a template's blocks + meta, captured on each publish.
 * {@link #contentSnapshotJson} holds {@code {"blocks": [...], "meta": {...}}}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "template_version", schema = "templates")
public class TemplateVersion {

  @Id
  private UUID id;

  @Column(name = "template_id", nullable = false)
  private UUID templateId;

  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "content_snapshot_json", nullable = false, columnDefinition = "jsonb")
  private String contentSnapshotJson;

  @Column(name = "published_at", nullable = false)
  private LocalDateTime publishedAt;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "change_note", length = 1000)
  private String changeNote;
}
