package org.sirantar.recadero.users.service.dto;

/**
 * Request payload for PATCH /api/v1/admin/users/{user_id}/status.
 */
public record UserStatusChangeRequest(String status, String reason) {}
