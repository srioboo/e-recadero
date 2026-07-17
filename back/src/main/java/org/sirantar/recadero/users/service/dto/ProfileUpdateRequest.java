package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for PUT /api/v1/users/me.
 */
public record ProfileUpdateRequest(ProfileFields profile) {

  public record ProfileFields(
      @JsonProperty("first_name") String firstName,
      @JsonProperty("last_name") String lastName,
      String phone,
      String bio,
      @JsonProperty("avatar_url") String avatarUrl,
      @JsonProperty("preferred_locale") String preferredLocale,
      @JsonProperty("newsletter_subscribed") Boolean newsletterSubscribed) {}
}
