package org.sirantar.recadero.users.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.users.domain.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for user role grants.
 */
public interface UserRoleRepository extends JpaRepository<UserRoleAssignment, Long> {

  List<UserRoleAssignment> findByUserId(Long userId);

  Optional<UserRoleAssignment> findByUserIdAndRoleName(Long userId, String roleName);

  void deleteByUserIdAndRoleName(Long userId, String roleName);
}
