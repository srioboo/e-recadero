package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response payload for GET/PUT /api/v1/users/me.
 */
public record UserProfileResponse(UserSummary user, ProfileDetail profile) {

  public record UserSummary(
      @JsonProperty("user_id") Long userId,
      String email,
      String username,
      String status,
      @JsonProperty("email_verified") boolean emailVerified,
      @JsonProperty("created_at") LocalDateTime createdAt) {}

  public record ProfileDetail(
      @JsonProperty("first_name") String firstName,
      @JsonProperty("last_name") String lastName,
      String phone,
      @JsonProperty("avatar_url") String avatarUrl,
      String bio,
      @JsonProperty("preferred_locale") String preferredLocale,
      @JsonProperty("preferred_currency") String preferredCurrency,
      @JsonProperty("newsletter_subscribed") boolean newsletterSubscribed) {}
}
