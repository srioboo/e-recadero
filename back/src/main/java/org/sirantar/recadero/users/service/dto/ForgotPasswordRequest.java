package org.sirantar.recadero.users.service.dto;

/**
 * Request payload for POST /api/v1/auth/forgot-password.
 */
public record ForgotPasswordRequest(String email) {}
