package org.sirantar.recadero.users.service.dto;

/**
 * Request payload for POST /api/v1/auth/verify-email.
 */
public record VerifyEmailRequest(String token) {}
