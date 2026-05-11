package org.sirantar.recadero.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom Spring Security principal representing authenticated user from JWT token.
 * 
 * Contains user identity information extracted from JWT claims:
 * - userId: UUID of authenticated user
 * - email: User's email address
 * - username: Login name (optional, may be same as email)
 * - roles: List of assigned roles (CUSTOMER, ADMIN, VENDOR, etc.)
 * - authorities: Spring Security GrantedAuthority instances (derived from roles)
 * 
 * Usage:
 * 1. In controllers: Inject as @AuthenticationPrincipal parameter
 *    @GetMapping("/me")
 *    public UserResponse getProfile(@AuthenticationPrincipal SecurityUser user) {
 *      return userService.getUser(user.getUserId());
 *    }
 * 
 * 2. In services: Use SecurityContextHolder to get current user
 *    SecurityUser user = SecurityUser.getCurrentUser();
 *    UUID userId = user.getUserId();
 * 
 * 3. In security expressions: @PreAuthorize("hasRole('ADMIN')")
 *    Uses authorities from this principal
 */
public class SecurityUser implements UserDetails, Serializable {

  private static final long serialVersionUID = 1L;

  private final String userId;
  private final String email;
  private final String username;
  private final List<String> roles;
  private final Collection<GrantedAuthority> authorities;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;

  /**
   * Construct SecurityUser from JWT claims and authorities.
   * 
   * @param userId        User ID (from JWT 'sub' claim)
   * @param email         User email (from JWT 'email' claim)
   * @param username      Username (from JWT, or email if not present)
   * @param roles         User roles (from JWT 'roles' claim)
   * @param authorities   Spring Security authorities (derived from roles)
   */
  public SecurityUser(
      String userId,
      String email,
      String username,
      List<String> roles,
      Collection<GrantedAuthority> authorities
  ) {
    this.userId = userId;
    this.email = email;
    this.username = username != null ? username : email;
    this.roles = roles != null ? roles : List.of();
    this.authorities = authorities;
    this.accountNonExpired = true;
    this.accountNonLocked = true;
    this.credentialsNonExpired = true;
    this.enabled = true;
  }

  /**
   * Get currently authenticated user from SecurityContext.
   * 
   * Returns the SecurityUser principal if user is authenticated.
   * 
   * @return              SecurityUser if authenticated, null otherwise
   * @throws IllegalStateException if principal is not SecurityUser
   */
  public static SecurityUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    
    if (principal instanceof SecurityUser) {
      return (SecurityUser) principal;
    }

    throw new IllegalStateException(
        "Expected SecurityUser principal but got: " + principal.getClass().getName()
    );
  }

  /**
   * Check if current user has a specific role.
   * 
   * @param role          Role name (e.g., "ADMIN", "VENDOR")
   * @return              true if user has role
   */
  public boolean hasRole(String role) {
    return roles.contains(role);
  }

  /**
   * Check if any of the provided roles match user's roles.
   * 
   * @param rolesArray    Role names to check
   * @return              true if user has any of the roles
   */
  public boolean hasAnyRole(String... rolesArray) {
    return roles.stream().anyMatch(r -> {
      for (String roleToCheck : rolesArray) {
        if (r.equals(roleToCheck)) {
          return true;
        }
      }
      return false;
    });
  }

  //=== Spring Security UserDetails Implementation ===

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    // JWT-based authentication doesn't use password
    return null;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  //=== Getters ===

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getRoles() {
    return roles;
  }

  @Override
  public String toString() {
    return "SecurityUser{" +
        "userId='" + userId + '\'' +
        ", email='" + email + '\'' +
        ", username='" + username + '\'' +
        ", roles=" + roles +
        ", enabled=" + enabled +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SecurityUser that = (SecurityUser) o;

    return userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return userId.hashCode();
  }
}
