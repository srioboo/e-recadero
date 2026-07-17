package org.sirantar.recadero.users.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.security.UserRole;
import org.sirantar.recadero.users.domain.UserRoleAssignment;
import org.sirantar.recadero.users.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grants and revokes {@link UserRole}s for a user.
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

  private final UserRoleRepository userRoleRepository;

  @Transactional
  public void grantRole(Long userId, String roleName, String grantedBy) {
    UserRole.fromValue(roleName);
    userRoleRepository.findByUserIdAndRoleName(userId, roleName).ifPresentOrElse(
        existing -> { },
        () -> {
          UserRoleAssignment assignment = new UserRoleAssignment();
          assignment.setUserId(userId);
          assignment.setRoleName(roleName);
          assignment.setGrantedAt(LocalDateTime.now());
          assignment.setGrantedBy(grantedBy);
          userRoleRepository.save(assignment);
        });
  }

  @Transactional
  public void revokeRole(Long userId, String roleName) {
    userRoleRepository.deleteByUserIdAndRoleName(userId, roleName);
  }

  public boolean hasRole(Long userId, String roleName) {
    return userRoleRepository.findByUserIdAndRoleName(userId, roleName).isPresent();
  }

  public List<String> getRoleNames(Long userId) {
    return userRoleRepository.findByUserId(userId).stream()
        .map(UserRoleAssignment::getRoleName)
        .toList();
  }
}
