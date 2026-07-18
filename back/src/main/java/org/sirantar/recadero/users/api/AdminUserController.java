package org.sirantar.recadero.users.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.service.AdminUserService;
import org.sirantar.recadero.users.service.UserRoleService;
import org.sirantar.recadero.users.service.dto.AdminUserDetail;
import org.sirantar.recadero.users.service.dto.AdminUserListItem;
import org.sirantar.recadero.users.service.dto.RoleGrantRequest;
import org.sirantar.recadero.users.service.dto.RoleGrantResponse;
import org.sirantar.recadero.users.service.dto.UserStatusChangeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only user account and role management.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@AdminOnly
public class AdminUserController {

  private final AdminUserService adminUserService;
  private final UserRoleService userRoleService;

  @GetMapping
  public PaginationResponse<AdminUserListItem> listUsers(
      @RequestParam(required = false) String status, Pageable pageable) {
    UserStatus filter = status != null ? UserStatus.valueOf(status) : null;
    return adminUserService.listUsers(filter, pageable);
  }

  @GetMapping("/{id}")
  public AdminUserDetail getUser(@PathVariable Long id) {
    return adminUserService.getUserDetail(id);
  }

  @PatchMapping("/{id}/status")
  public AdminUserDetail changeStatus(@PathVariable Long id, @RequestBody UserStatusChangeRequest request) {
    return adminUserService.changeStatus(id, request.status());
  }

  @PostMapping("/{id}/roles")
  @ResponseStatus(HttpStatus.CREATED)
  public RoleGrantResponse grantRole(
      @PathVariable Long id, @RequestBody RoleGrantRequest request, @AuthenticationPrincipal SecurityUser admin) {
    userRoleService.grantRole(id, request.roleName(), admin.getUserId());
    return new RoleGrantResponse(id, userRoleService.getRoleNames(id));
  }

  @DeleteMapping("/{id}/roles/{roleName}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeRole(@PathVariable Long id, @PathVariable String roleName) {
    userRoleService.revokeRole(id, roleName);
  }
}
