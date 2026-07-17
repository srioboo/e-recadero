package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for POST /api/v1/auth/register.
 */
public record RegisterResponse(
    @JsonProperty("user_id") Long userId,
    String email,
    String username,
    @JsonProperty("email_verified") boolean emailVerified,
    String message) {}
