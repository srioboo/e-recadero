package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for PUT /api/v1/users/me/password.
 */
public record ChangePasswordRequest(
    @JsonProperty("current_password") String currentPassword,
    @JsonProperty("new_password") String newPassword) {}
