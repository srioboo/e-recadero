package org.sirantar.recadero.users.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.security.Authenticated;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.sirantar.recadero.users.service.UserProfileService;
import org.sirantar.recadero.users.service.dto.ChangePasswordRequest;
import org.sirantar.recadero.users.service.dto.DeactivateAccountRequest;
import org.sirantar.recadero.users.service.dto.MessageResponse;
import org.sirantar.recadero.users.service.dto.ProfileUpdateRequest;
import org.sirantar.recadero.users.service.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user's own profile management.
 */
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Authenticated
public class UserProfileController {

  private final UserProfileService userProfileService;

  @GetMapping
  public UserProfileResponse getProfile(@AuthenticationPrincipal SecurityUser user) {
    return userProfileService.getProfile(Long.valueOf(user.getUserId()));
  }

  @PutMapping
  public UserProfileResponse updateProfile(
      @AuthenticationPrincipal SecurityUser user, @RequestBody ProfileUpdateRequest request) {
    return userProfileService.updateProfile(Long.valueOf(user.getUserId()), request);
  }

  @PutMapping("/password")
  public MessageResponse changePassword(
      @AuthenticationPrincipal SecurityUser user, @RequestBody ChangePasswordRequest request) {
    userProfileService.changePassword(Long.valueOf(user.getUserId()), request.currentPassword(), request.newPassword());
    return new MessageResponse("Password changed successfully");
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivateAccount(
      @AuthenticationPrincipal SecurityUser user, @RequestBody DeactivateAccountRequest request) {
    userProfileService.deactivateAccount(Long.valueOf(user.getUserId()), request.password());
  }
}
