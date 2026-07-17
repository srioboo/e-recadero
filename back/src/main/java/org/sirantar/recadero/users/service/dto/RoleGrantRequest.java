package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/admin/users/{user_id}/roles.
 */
public record RoleGrantRequest(@JsonProperty("role_name") String roleName) {}
