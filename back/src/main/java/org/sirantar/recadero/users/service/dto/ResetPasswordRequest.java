package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/auth/reset-password.
 */
public record ResetPasswordRequest(String token, @JsonProperty("new_password") String newPassword) {}
