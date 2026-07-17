package org.sirantar.recadero.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A role granted to a user (denormalized role name, see users.user_role_assignment).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_role_assignment", schema = "users")
public class UserRoleAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "role_name", nullable = false, length = 20)
  private String roleName;

  @Column(name = "assigned_at", nullable = false, updatable = false)
  private LocalDateTime grantedAt;

  @Column(name = "assigned_by", length = 255)
  private String grantedBy;
}
