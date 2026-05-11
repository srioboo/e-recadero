package org.sirantar.recadero.shared.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Custom Spring Security Expression Language (SpEL) functions for use in @PreAuthorize and @PostAuthorize.
 * 
 * These methods provide cleaner, more readable security expressions compared to inline SpEL.
 * 
 * Usage in @PreAuthorize and @PostAuthorize annotations:
 * 
 * // Check if current user is admin
 * @PreAuthorize("securityExpressions.isAdmin()")
 * public void deleteProduct(UUID productId) { ... }
 * 
 * // Check if current user owns the resource
 * @PreAuthorize("securityExpressions.isOwner(#userId)")
 * public UserResponse getProfile(@PathVariable UUID userId) { ... }
 * 
 * // Check if current user has only customer role
 * @PreAuthorize("securityExpressions.isCustomerOnly()")
 * public void addToCart(UUID productId) { ... }
 * 
 * // Combine with other expressions
 * @PostAuthorize("securityExpressions.isOwner(returnObject.userId) or securityExpressions.isAdmin()")
 * public OrderResponse getOrder(UUID orderId) { ... }
 * 
 * To register these expressions in the security configuration, add this bean to SecurityConfig:
 * 
 * @Bean
 * public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
 *   DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
 *   handler.setRolePrefix("");  // Remove "ROLE_" prefix since we use role names directly
 *   return handler;
 * }
 * 
 * Note: SpEL context is available via #root.securityExpressions when registered via SecurityConfig
 */
@Component("securityExpressions")
public class SecurityExpressionRoot {

  /**
   * Check if current user is authenticated.
   * 
   * @return true if user is authenticated
   */
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated();
  }

  /**
   * Check if current user is ADMIN.
   * 
   * @return true if user has ADMIN role
   */
  public boolean isAdmin() {
    SecurityUser user = getCurrentUser();
    return user != null && user.hasRole(UserRole.ADMIN.value());
  }

  /**
   * Check if current user is CUSTOMER (and only customer).
   * 
   * @return true if user's only role is CUSTOMER
   */
  public boolean isCustomerOnly() {
    SecurityUser user = getCurrentUser();
    return user != null && 
           user.getRoles().size() == 1 && 
           user.hasRole(UserRole.CUSTOMER.value());
  }

  /**
   * Check if current user has ADMIN or SUPPORT role.
   * Used for customer service operations.
   * 
   * @return true if user is admin or support staff
   */
  public boolean isAdminOrSupport() {
    SecurityUser user = getCurrentUser();
    return user != null && 
           (user.hasRole(UserRole.ADMIN.value()) || user.hasRole(UserRole.SUPPORT.value()));
  }

  /**
   * Check if current user has ADMIN or VENDOR role.
   * Used for content management operations.
   * 
   * @return true if user is admin or vendor
   */
  public boolean isAdminOrVendor() {
    SecurityUser user = getCurrentUser();
    return user != null && 
           (user.hasRole(UserRole.ADMIN.value()) || user.hasRole(UserRole.VENDOR.value()));
  }

  /**
   * Check if current user has any of the specified roles.
   * 
   * @param roles role names to check (e.g., "ADMIN", "VENDOR")
   * @return true if user has any of the specified roles
   */
  public boolean hasAnyRole(String... roles) {
    SecurityUser user = getCurrentUser();
    if (user == null) {
      return false;
    }
    
    for (String role : roles) {
      if (user.hasRole(role)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if current user has all specified roles.
   * 
   * @param roles role names to check
   * @return true if user has all specified roles
   */
  public boolean hasAllRoles(String... roles) {
    SecurityUser user = getCurrentUser();
    if (user == null) {
      return false;
    }
    
    for (String role : roles) {
      if (!user.hasRole(role)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if current user owns the specified resource.
   * Used for @PreAuthorize("securityExpressions.isOwner(#userId)")
   * 
   * @param resourceOwnerId UUID of resource owner
   * @return true if current user owns the resource or is admin
   */
  public boolean isOwner(UUID resourceOwnerId) {
    if (resourceOwnerId == null) {
      return false;
    }
    
    SecurityUser user = getCurrentUser();
    if (user == null) {
      return false;
    }
    
    // Allow access if user owns resource OR is admin
    return UUID.fromString(user.getUserId()).equals(resourceOwnerId) || isAdmin();
  }

  /**
   * Check if current user owns the specified resource (String userId).
   * Used for @PreAuthorize("securityExpressions.isOwner(#userId)")
   * 
   * @param resourceOwnerId String UUID of resource owner
   * @return true if current user owns the resource or is admin
   */
  public boolean isOwner(String resourceOwnerId) {
    if (resourceOwnerId == null) {
      return false;
    }
    
    try {
      return isOwner(UUID.fromString(resourceOwnerId));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Check if current user owns the resource but deny admin override.
   * Used when strict ownership is required and admin cannot override.
   * 
   * @param resourceOwnerId UUID of resource owner
   * @return true only if current user owns the resource
   */
  public boolean isOwnerStrict(UUID resourceOwnerId) {
    if (resourceOwnerId == null) {
      return false;
    }
    
    SecurityUser user = getCurrentUser();
    return user != null && UUID.fromString(user.getUserId()).equals(resourceOwnerId);
  }

  /**
   * Check if current user is the specified user by ID.
   * Used for operations like "get my profile" where you need exact user ID match.
   * 
   * @param userId UUID of user to match
   * @return true if current user's ID matches the given userId
   */
  public boolean isCurrentUser(UUID userId) {
    if (userId == null) {
      return false;
    }
    
    SecurityUser user = getCurrentUser();
    return user != null && UUID.fromString(user.getUserId()).equals(userId);
  }

  /**
   * Check if current user has admin role OR owns the resource.
   * Used for operations like "view order: admin can view any, customers view own".
   * 
   * @param resourceOwnerId UUID of resource owner
   * @return true if user is admin or owns resource
   */
  public boolean isAdminOrOwner(UUID resourceOwnerId) {
    return isAdmin() || isOwner(resourceOwnerId);
  }

  /**
   * Get the UUID of the currently authenticated user.
   * Returns null if not authenticated.
   * 
   * @return current user's UUID or null
   */
  public UUID getCurrentUserId() {
    SecurityUser user = getCurrentUser();
    return user != null ? UUID.fromString(user.getUserId()) : null;
  }

  // ==================== Helper Methods ====================

  /**
   * Get current user from SecurityContext.
   * 
   * @return SecurityUser if authenticated, null otherwise
   */
  private SecurityUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof SecurityUser) {
      return (SecurityUser) principal;
    }

    return null;
  }
}
