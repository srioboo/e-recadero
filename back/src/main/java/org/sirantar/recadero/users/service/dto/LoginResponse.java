package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response payload for POST /api/v1/auth/login.
 */
public record LoginResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") int expiresIn,
    LoginUser user) {

  public record LoginUser(
      @JsonProperty("user_id") Long userId,
      String email,
      String username,
      List<String> roles,
      LoginProfile profile) {}

  public record LoginProfile(
      @JsonProperty("first_name") String firstName,
      @JsonProperty("last_name") String lastName,
      @JsonProperty("avatar_url") String avatarUrl,
      @JsonProperty("preferred_locale") String preferredLocale) {}
}
