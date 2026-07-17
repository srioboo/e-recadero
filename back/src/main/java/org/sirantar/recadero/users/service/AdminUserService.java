package org.sirantar.recadero.users.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.users.domain.AddressType;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.service.dto.AdminUserDetail;
import org.sirantar.recadero.users.service.dto.AdminUserListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-only user management: listing, detail lookup, and status changes.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final UserRoleService userRoleService;
  private final UserProfileService userProfileService;
  private final AddressService addressService;

  public PaginationResponse<AdminUserListItem> listUsers(UserStatus status, Pageable pageable) {
    Page<User> page = status != null
        ? userRepository.findByStatus(status, pageable)
        : userRepository.findAll(pageable);
    return PaginationResponse.from(page.map(this::toListItem));
  }

  public AdminUserDetail getUserDetail(Long userId) {
    User user = getUser(userId);
    var profile = userProfileService.getProfile(userId);
    List<org.sirantar.recadero.users.service.dto.AddressResponse> addresses =
        addressService.listAddresses(userId, (AddressType) null);
    return new AdminUserDetail(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getStatus().name(),
        Boolean.TRUE.equals(user.getEmailVerified()),
        userRoleService.getRoleNames(userId),
        user.getCreatedAt(),
        user.getLastLoginAt(),
        profile.profile(),
        addresses);
  }

  @Transactional
  public AdminUserDetail changeStatus(Long userId, String status) {
    User user = getUser(userId);
    user.setStatus(UserStatus.valueOf(status));
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
    return getUserDetail(userId);
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
  }

  private AdminUserListItem toListItem(User user) {
    return new AdminUserListItem(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getStatus().name(),
        userRoleService.getRoleNames(user.getId()),
        user.getCreatedAt(),
        user.getLastLoginAt());
  }
}
