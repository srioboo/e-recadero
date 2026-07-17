package org.sirantar.recadero.users.service.dto;

/**
 * Request payload for POST /api/v1/auth/login.
 */
public record LoginRequest(String email, String password) {}
