package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Row shape for GET /api/v1/admin/users.
 */
public record AdminUserListItem(
    @JsonProperty("user_id") Long userId,
    String email,
    String username,
    String status,
    List<String> roles,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("last_login_at") LocalDateTime lastLoginAt) {}
