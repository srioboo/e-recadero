package org.sirantar.recadero.users.service.dto;

/**
 * Request payload for DELETE /api/v1/users/me.
 */
public record DeactivateAccountRequest(String password, String reason) {}
