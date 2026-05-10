package org.sirantar.recadero.shared.config;

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
 * Extracts roles from JWT claims and creates an AuthenticationToken with:
 * - Principal: JWT subject (user_id)
 * - Credentials: JWT token
 * - Authorities: Roles from JWT "roles" claim (or "scope" for standard OAuth2)
 * 
 * Called by Spring Security OAuth2 Resource Server during token validation.
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  static final String ROLES_CLAIM = "roles";
  static final String SCOPE_CLAIM = "scope";
  static final String ROLE_PREFIX = "ROLE_";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    String principalName = jwt.getClaimAsString("sub");
    
    return new UsernamePasswordAuthenticationToken(
        principalName,
        jwt.getTokenValue(),
        authorities
    );
  }

  /**
   * Extract authorities from JWT claims.
   * 
   * Supports two claim formats:
   * 1. "roles": ["CUSTOMER", "ADMIN"] (custom claim)
   * 2. "scope": "openid profile email" (standard OAuth2, space-separated)
   * 
   * Converts to Spring authorities with "ROLE_" prefix if not present.
   */
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();

    // Check for custom "roles" claim first
    if (jwt.hasClaim(ROLES_CLAIM)) {
      List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
      authorities.addAll(
          roles.stream()
              .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toSet())
      );
    }
    
    // Fallback to standard "scope" claim (space-separated)
    if (jwt.hasClaim(SCOPE_CLAIM)) {
      String scopes = jwt.getClaimAsString(SCOPE_CLAIM);
      if (scopes != null && !scopes.isEmpty()) {
        authorities.addAll(
            Arrays.stream(scopes.split(" "))
                .map(scope -> scope.startsWith(ROLE_PREFIX) ? scope : ROLE_PREFIX + scope)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet())
        );
      }
    }

    return authorities;
  }
}
