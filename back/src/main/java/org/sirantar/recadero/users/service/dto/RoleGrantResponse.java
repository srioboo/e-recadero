package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response payload for POST /api/v1/admin/users/{user_id}/roles.
 */
public record RoleGrantResponse(@JsonProperty("user_id") Long userId, List<String> roles) {}
