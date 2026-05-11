package org.sirantar.recadero.shared.security;

/**
 * Common permission and role-based security expressions for use in @PreAuthorize and @PostAuthorize annotations.
 * 
 * These constants represent common security patterns across the application:
 * - Role checks: hasRole('ADMIN'), hasRole('CUSTOMER')
 * - Role combinations: hasAnyRole('ADMIN', 'SUPPORT')
 * - Custom expressions: isAuthenticated()
 * 
 * Usage in controllers/services:
 * 
 * @PreAuthorize(IS_ADMIN)
 * public void deleteCategory(UUID id) { ... }
 * 
 * @PreAuthorize(HAS_ADMIN_OR_SUPPORT_ROLE)
 * public OrderResponse getAnyOrder(UUID orderId) { ... }
 * 
 * @PreAuthorize(IS_AUTHENTICATED)
 * public UserResponse getProfile() { ... }
 * 
 * @PostAuthorize(CAN_VIEW_ENTITY + " or hasRole('ADMIN')")
 * public OrderResponse getOrder(UUID orderId) { ... }
 */
public final class PermissionConstants {
  
  // ==================== Role-Based Permissions ====================
  
  /**
   * Admin-only access. Used for system-wide operations.
   * Expression: hasRole('ADMIN')
   */
  public static final String IS_ADMIN = "hasRole('ADMIN')";

  /**
   * Admin or vendor access. Used for storefront management.
   * Expression: hasAnyRole('ADMIN', 'VENDOR')
   */
  public static final String HAS_ADMIN_OR_VENDOR_ROLE = "hasAnyRole('ADMIN', 'VENDOR')";

  /**
   * Admin or support access. Used for customer service operations.
   * Expression: hasAnyRole('ADMIN', 'SUPPORT')
   */
  public static final String HAS_ADMIN_OR_SUPPORT_ROLE = "hasAnyRole('ADMIN', 'SUPPORT')";

  /**
   * Admin, vendor, or support access. Used for privileged operations.
   * Expression: hasAnyRole('ADMIN', 'VENDOR', 'SUPPORT')
   */
  public static final String HAS_ADMIN_VENDOR_OR_SUPPORT_ROLE = "hasAnyRole('ADMIN', 'VENDOR', 'SUPPORT')";

  /**
   * Customer-only access. Used for customer-specific operations.
   * Expression: hasRole('CUSTOMER')
   */
  public static final String IS_CUSTOMER = "hasRole('CUSTOMER')";

  /**
   * Analyst access. Used for analytics and reporting.
   * Expression: hasRole('ANALYST')
   */
  public static final String IS_ANALYST = "hasRole('ANALYST')";

  /**
   * Analyst or admin access. Used for reporting with admin capabilities.
   * Expression: hasAnyRole('ANALYST', 'ADMIN')
   */
  public static final String HAS_ADMIN_OR_ANALYST_ROLE = "hasAnyRole('ANALYST', 'ADMIN')";

  // ==================== General Access Permissions ====================

  /**
   * General authentication check.
   * Expression: isAuthenticated()
   */
  public static final String IS_AUTHENTICATED = "isAuthenticated()";

  /**
   * Allow anonymous access (no authentication required).
   * Expression: permitAll()
   */
  public static final String IS_ANONYMOUS = "permitAll()";

  /**
   * Deny all access.
   * Expression: denyAll()
   */
  public static final String DENY_ALL = "denyAll()";

  // ==================== Data Ownership Patterns ====================

  /**
   * User can access own profile only.
   * SpEL expression: Use #userId or #id from request parameter
   * 
   * Example:
   * @PreAuthorize("hasRole('CUSTOMER') and #userId == authentication.principal.userId")
   * public UserResponse getUser(@PathVariable UUID userId) { ... }
   */
  public static final String OWN_RESOURCE_ONLY_PREFIX = "hasRole('CUSTOMER') and #";

  /**
   * User can access own data or has admin role.
   * SpEL expression: Combines ownership check with admin override
   * 
   * Example:
   * @PostAuthorize("returnObject.userId == authentication.principal.userId or hasRole('ADMIN')")
   * public OrderResponse getOrder(UUID orderId) { ... }
   */
  public static final String OWN_RESOURCE_OR_ADMIN = "or hasRole('ADMIN')";

  // ==================== API Endpoint Patterns ====================

  /**
   * Public endpoints (no authentication required).
   * Used in SecurityConfig HttpSecurity.authorizeHttpRequests()
   */
  public static final String PUBLIC_API = "permitAll()";

  /**
   * Protected endpoints (authentication required).
   * Used in SecurityConfig HttpSecurity.authorizeHttpRequests()
   */
  public static final String PROTECTED_API = "authenticated()";

  /**
   * Admin-only API endpoints.
   * Used in SecurityConfig HttpSecurity.authorizeHttpRequests()
   */
  public static final String ADMIN_API = "hasRole('ADMIN')";

  // Prevent instantiation
  private PermissionConstants() {
    throw new AssertionError("Cannot instantiate PermissionConstants");
  }
}
