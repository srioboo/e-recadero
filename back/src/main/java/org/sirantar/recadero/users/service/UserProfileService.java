package org.sirantar.recadero.users.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserProfile;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.events.UserEventPublisher;
import org.sirantar.recadero.users.repository.UserProfileRepository;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.service.dto.ProfileUpdateRequest;
import org.sirantar.recadero.users.service.dto.UserProfileResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the extended profile, password, and lifecycle of a user account.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserEventPublisher eventPublisher;

  @Transactional
  public UserProfile createProfile(Long userId) {
    UserProfile profile = new UserProfile();
    profile.setUserId(userId);
    LocalDateTime now = LocalDateTime.now();
    profile.setCreatedAt(now);
    profile.setUpdatedAt(now);
    return userProfileRepository.save(profile);
  }

  public UserProfileResponse getProfile(Long userId) {
    User user = getUser(userId);
    UserProfile profile = getOrCreateProfile(userId);
    return toResponse(user, profile);
  }

  @Transactional
  public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
    User user = getUser(userId);
    UserProfile profile = getOrCreateProfile(userId);
    ProfileUpdateRequest.ProfileFields fields = request.profile();
    List<String> changed = new ArrayList<>();

    if (fields.firstName() != null && !fields.firstName().equals(profile.getFirstName())) {
      profile.setFirstName(fields.firstName());
      changed.add("first_name");
    }
    if (fields.lastName() != null && !fields.lastName().equals(profile.getLastName())) {
      profile.setLastName(fields.lastName());
      changed.add("last_name");
    }
    if (fields.phone() != null) {
      profile.setPhoneNumber(fields.phone());
      changed.add("phone");
    }
    if (fields.bio() != null) {
      profile.setBio(fields.bio());
      changed.add("bio");
    }
    if (fields.avatarUrl() != null) {
      profile.setAvatarUrl(fields.avatarUrl());
      changed.add("avatar_url");
    }
    if (fields.preferredLocale() != null && !fields.preferredLocale().equals(profile.getPreferredLocale())) {
      profile.setPreferredLocale(fields.preferredLocale());
      changed.add("preferred_locale");
    }
    if (fields.newsletterSubscribed() != null) {
      profile.setNewsletterSubscribed(fields.newsletterSubscribed());
      changed.add("newsletter_subscribed");
    }

    profile.setUpdatedAt(LocalDateTime.now());
    UserProfile saved = userProfileRepository.save(profile);

    if (!changed.isEmpty()) {
      eventPublisher.publishProfileUpdated(userId, changed);
    }

    return toResponse(user, saved);
  }

  @Transactional
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user = getUser(userId);
    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new BadCredentialsException("Current password is incorrect");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setPasswordChangedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
  }

  @Transactional
  public void deactivateAccount(Long userId, String password) {
    User user = getUser(userId);
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new BadCredentialsException("Password is incorrect");
    }
    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new ResourceConflictException("Cannot deactivate a SUSPENDED user");
    }
    user.setStatus(UserStatus.INACTIVE);
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
  }

  private UserProfile getOrCreateProfile(Long userId) {
    return userProfileRepository.findByUserId(userId).orElseGet(() -> createProfile(userId));
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
  }

  private UserProfileResponse toResponse(User user, UserProfile profile) {
    return new UserProfileResponse(
        new UserProfileResponse.UserSummary(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getStatus().name(),
            Boolean.TRUE.equals(user.getEmailVerified()),
            user.getCreatedAt()),
        new UserProfileResponse.ProfileDetail(
            profile.getFirstName(),
            profile.getLastName(),
            profile.getPhoneNumber(),
            profile.getAvatarUrl(),
            profile.getBio(),
            profile.getPreferredLocale(),
            profile.getPreferredCurrency(),
            Boolean.TRUE.equals(profile.getNewsletterSubscribed())));
  }
}
