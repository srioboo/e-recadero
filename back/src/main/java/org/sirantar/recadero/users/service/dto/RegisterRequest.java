package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/auth/register.
 */
public record RegisterRequest(
    String email,
    String username,
    String password,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName) {}
