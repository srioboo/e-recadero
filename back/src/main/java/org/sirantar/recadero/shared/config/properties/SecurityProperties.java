package org.sirantar.recadero.shared.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application security configuration properties.
 * 
 * Loaded from application.yml under "app.security" prefix.
 * 
 * Example:
 * app:
 *   security:
 *     cors:
 *       allowed-origins: http://localhost:3000,http://localhost:3001
 *     jwt:
 *       issuer: http://localhost:8080
 *       secret: changeme-secret
 *       access-token-expiry: 900
 *       refresh-token-expiry: 604800
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

  private Cors cors = new Cors();
  private Jwt jwt = new Jwt();

  public static class Cors {
    private String allowedOrigins = "http://localhost:3000,http://localhost:3001";

    public String getAllowedOrigins() {
      return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
      this.allowedOrigins = allowedOrigins;
    }
  }

  public static class Jwt {
    private String issuer = "http://localhost:8080";
    private String secret = "changeme-this-is-default-secret-key-use-env-var-in-prod";
    private int accessTokenExpiry = 900;  // 15 minutes
    private int refreshTokenExpiry = 604800;  // 7 days

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public int getAccessTokenExpiry() {
      return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(int accessTokenExpiry) {
      this.accessTokenExpiry = accessTokenExpiry;
    }

    public int getRefreshTokenExpiry() {
      return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(int refreshTokenExpiry) {
      this.refreshTokenExpiry = refreshTokenExpiry;
    }
  }

  public Cors getCors() {
    return cors;
  }

  public void setCors(Cors cors) {
    this.cors = cors;
  }

  public Jwt getJwt() {
    return jwt;
  }

  public void setJwt(Jwt jwt) {
    this.jwt = jwt;
  }
}
