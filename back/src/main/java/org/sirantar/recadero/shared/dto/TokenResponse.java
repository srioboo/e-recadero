package org.sirantar.recadero.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Token response DTO returned from authentication endpoints.
 * 
 * Contains:
 * - access_token: Short-lived JWT for API requests (15 min)
 * - refresh_token: Long-lived JWT for token refresh (7 days)
 * - expires_in: Seconds until access token expiry
 * - token_type: Bearer (OAuth2 standard)
 * 
 * Example Response:
 * {
 *   "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "expires_in": 900,
 *   "token_type": "Bearer"
 * }
 */
public class TokenResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("expires_in")
  private Integer expiresIn;

  @JsonProperty("token_type")
  private String tokenType = "Bearer";

  public TokenResponse() {
  }

  public TokenResponse(String accessToken, String refreshToken, Integer expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
    this.tokenType = "Bearer";
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  @Override
  public String toString() {
    return "TokenResponse{" +
        "accessToken='" + (accessToken != null ? accessToken.substring(0, 20) + "..." : null) + '\'' +
        ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, 20) + "..." : null) + '\'' +
        ", expiresIn=" + expiresIn +
        ", tokenType='" + tokenType + '\'' +
        '}';
  }
}
