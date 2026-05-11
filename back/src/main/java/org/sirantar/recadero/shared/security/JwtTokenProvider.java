package org.sirantar.recadero.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.sirantar.recadero.shared.config.properties.SecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT Token Provider for token generation, validation, and claims extraction.
 * 
 * Features:
 * - Generate access tokens (15 min expiry)
 * - Generate refresh tokens (7 day expiry)
 * - Validate token signatures and expiry
 * - Extract claims (subject, email, roles, expiry)
 * - Support custom claims (roles, email, etc.)
 * 
 * Token Structure:
 * Header: {alg: HS256, typ: JWT}
 * Payload: {sub: user_id, email: user_email, roles: [CUSTOMER, ...], iat, exp, aud}
 * Signature: HMAC-SHA256(secret_key)
 */
@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final SecurityProperties securityProperties;

  public JwtTokenProvider(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
    this.key = Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(securityProperties.getJwt().getSecret())
    );
  }

  /**
   * Generate access token (short-lived, 15 minutes).
   * 
   * @param userId        Subject claim (user ID)
   * @param email         User email for identification
   * @param roles         User roles (CUSTOMER, ADMIN, VENDOR, etc.)
   * @return              JWT access token
   */
  public String generateAccessToken(String userId, String email, List<String> roles) {
    Date now = new Date();
    Date expiryDate = new Date(
        now.getTime() + (securityProperties.getJwt().getAccessTokenExpiry() * 1000L)
    );

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .setIssuer(securityProperties.getJwt().getIssuer())
        .setAudience("recadero-api")
        .claim("email", email)
        .claim("roles", roles)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Generate refresh token (long-lived, 7 days).
   * 
   * Used to obtain new access tokens without re-authentication.
   * Should be stored in Redis with expiry for revocation support.
   * 
   * @param userId        Subject claim (user ID)
   * @return              JWT refresh token
   */
  public String generateRefreshToken(String userId) {
    Date now = new Date();
    Date expiryDate = new Date(
        now.getTime() + (securityProperties.getJwt().getRefreshTokenExpiry() * 1000L)
    );

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .setIssuer(securityProperties.getJwt().getIssuer())
        .setAudience("recadero-api")
        .claim("type", "refresh")
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validate token signature and expiry.
   * 
   * @param token         JWT token to validate
   * @return              true if token is valid, false otherwise
   * @throws JwtException if token format is invalid
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Extract all claims from token.
   * 
   * @param token         JWT token
   * @return              Claims object containing all token claims
   * @throws JwtException if token is invalid or expired
   */
  public Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Extract subject (user ID) from token.
   * 
   * @param token         JWT token
   * @return              Subject (user ID)
   */
  public String extractSubject(String token) {
    return extractAllClaims(token).getSubject();
  }

  /**
   * Extract email from token claims.
   * 
   * @param token         JWT token
   * @return              User email
   */
  public String extractEmail(String token) {
    return (String) extractAllClaims(token).get("email");
  }

  /**
   * Extract roles from token claims.
   * 
   * @param token         JWT token
   * @return              List of role names
   */
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    return (List<String>) extractAllClaims(token).get("roles");
  }

  /**
   * Extract expiration time from token.
   * 
   * @param token         JWT token
   * @return              Expiration date
   */
  public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }

  /**
   * Check if token is expired.
   * 
   * @param token         JWT token
   * @return              true if expired, false otherwise
   */
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extract seconds until token expiration.
   * 
   * @param token         JWT token
   * @return              Seconds remaining until expiry (0 if expired)
   */
  public long getExpirationTimeInSeconds(String token) {
    long expirationTime = extractExpiration(token).getTime();
    long currentTime = System.currentTimeMillis();
    long remainingTime = expirationTime - currentTime;
    return Math.max(0, remainingTime / 1000);
  }

  /**
   * Get access token expiry duration in seconds.
   * 
   * Used by clients to know when to refresh tokens.
   * 
   * @return              Access token expiry in seconds
   */
  public int getAccessTokenExpiry() {
    return securityProperties.getJwt().getAccessTokenExpiry();
  }

  /**
   * Get refresh token expiry duration in seconds.
   * 
   * @return              Refresh token expiry in seconds
   */
  public int getRefreshTokenExpiry() {
    return securityProperties.getJwt().getRefreshTokenExpiry();
  }
}
