package org.sirantar.recadero.shared.config;

import org.sirantar.recadero.shared.security.SecurityUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter from JWT to Spring Security Authentication.
 * 
 * Extracts claims from JWT and creates SecurityUser principal with:
 * - userId: JWT subject (user_id)
 * - email: User email from JWT claim
 * - username: Login name (from JWT or falls back to email)
 * - roles: User roles from JWT "roles" claim
 * - authorities: Spring Security GrantedAuthority instances
 * 
 * Called by Spring Security OAuth2 Resource Server during token validation.
 * 
 * Result can be used as:
 * - @AuthenticationPrincipal SecurityUser in controllers
 * - SecurityUser.getCurrentUser() in services
 * - @PreAuthorize("hasRole('ADMIN')") in security expressions
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  static final String ROLES_CLAIM = "roles";
  static final String SCOPE_CLAIM = "scope";
  static final String ROLE_PREFIX = "ROLE_";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    // Extract all claims
    String userId = jwt.getClaimAsString("sub");
    String email = jwt.getClaimAsString("email");
    String username = jwt.getClaimAsString("username");
    List<String> roles = extractRoles(jwt);
    Collection<GrantedAuthority> authorities = extractAuthorities(roles);

    // Create SecurityUser principal
    SecurityUser principal = new SecurityUser(
        userId,
        email,
        username != null ? username : email,
        roles,
        authorities
    );

    // Return authentication token with SecurityUser as principal
    return new UsernamePasswordAuthenticationToken(
        principal,
        jwt.getTokenValue(),
        authorities
    );
  }

  /**
   * Extract roles from JWT claims.
   * 
   * Supports two claim formats:
   * 1. "roles": ["CUSTOMER", "ADMIN"] (custom claim, preferred)
   * 2. "scope": "openid profile email" (standard OAuth2, space-separated)
   * 
   * @param jwt   JWT token
   * @return      List of role names
   */
  private List<String> extractRoles(Jwt jwt) {
    // Check for custom "roles" claim first
    if (jwt.hasClaim(ROLES_CLAIM)) {
      return jwt.getClaimAsStringList(ROLES_CLAIM);
    }
    
    // Fallback to standard "scope" claim (space-separated)
    if (jwt.hasClaim(SCOPE_CLAIM)) {
      String scopes = jwt.getClaimAsString(SCOPE_CLAIM);
      if (scopes != null && !scopes.isEmpty()) {
        return Arrays.asList(scopes.split(" "));
      }
    }

    return List.of();
  }

  /**
   * Convert role names to Spring Security GrantedAuthority instances.
   * 
   * Adds "ROLE_" prefix to each role if not already present
   * (Spring Security convention for role-based access control).
   * 
   * @param roles List of role names
   * @return      Collection of GrantedAuthority
   */
  private Collection<GrantedAuthority> extractAuthorities(List<String> roles) {
    return roles.stream()
        .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }
}
