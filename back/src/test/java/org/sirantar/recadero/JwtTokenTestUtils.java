package org.sirantar.recadero;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Utility methods for generating JWT tokens in tests.
 */
public final class JwtTokenTestUtils {

  private static final String DEFAULT_ISSUER = "http://localhost:8080";
  private static final String DEFAULT_AUDIENCE = "recadero-api";
  private static final String DEFAULT_SECRET =
      "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
  private static final int DEFAULT_ACCESS_TOKEN_EXPIRY_SECONDS = 900;
  private static final int DEFAULT_REFRESH_TOKEN_EXPIRY_SECONDS = 604800;

  private JwtTokenTestUtils() {
  }

  public static String generateAccessToken(String userId, String email, List<String> roles) {
    return generateAccessToken(
        userId,
        email,
        roles,
        DEFAULT_ISSUER,
        DEFAULT_SECRET,
        DEFAULT_ACCESS_TOKEN_EXPIRY_SECONDS
    );
  }

  public static String generateAccessToken(
      String userId,
      String email,
      List<String> roles,
      String issuer,
      String secret,
      int expirySeconds
  ) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + (expirySeconds * 1000L));
    SecretKey key = signingKey(secret);

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .setIssuer(issuer)
        .setAudience(DEFAULT_AUDIENCE)
        .claim("email", email)
        .claim("roles", roles)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public static String generateAccessToken(String userId, String email, String... roles) {
    return generateAccessToken(userId, email, List.of(roles));
  }

  public static String generateRefreshToken(String userId) {
    return generateRefreshToken(
        userId,
        DEFAULT_ISSUER,
        DEFAULT_SECRET,
        DEFAULT_REFRESH_TOKEN_EXPIRY_SECONDS
    );
  }

  public static String generateRefreshToken(
      String userId,
      String issuer,
      String secret,
      int expirySeconds
  ) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + (expirySeconds * 1000L));
    SecretKey key = signingKey(secret);

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .setIssuer(issuer)
        .setAudience(DEFAULT_AUDIENCE)
        .claim("type", "refresh")
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  private static SecretKey signingKey(String base64Secret) {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
  }
}
