package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full detail shape for GET /api/v1/admin/users/{user_id}.
 */
public record AdminUserDetail(
    @JsonProperty("user_id") Long userId,
    String email,
    String username,
    String status,
    @JsonProperty("email_verified") boolean emailVerified,
    List<String> roles,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("last_login_at") LocalDateTime lastLoginAt,
    UserProfileResponse.ProfileDetail profile,
    List<AddressResponse> addresses) {}
