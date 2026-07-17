package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for POST /api/v1/auth/refresh-token.
 */
public record RefreshTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") int expiresIn) {}
