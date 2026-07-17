package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response payload for POST /api/v1/auth/verify-email.
 */
public record VerifyEmailResponse(@JsonProperty("user_id") Long userId, String message) {}
