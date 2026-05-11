package org.sirantar.recadero.shared.security;

/**
 * Enumeration of user roles in the Recadero e-commerce system.
 * 
 * Roles are assigned to users and used in:
 * - JWT token generation (claims)
 * - Spring Security access control annotations (@PreAuthorize, @PostAuthorize)
 * - API endpoint authorization rules
 * - Service-level business logic decisions
 * 
 * Role Hierarchy (from least to most privileged):
 * 1. CUSTOMER: Regular customers; can view products, create orders, access own data
 * 2. VENDOR: Sellers who manage their storefront; extended product/order management
 * 3. SUPPORT: Customer support staff; can view/modify customer accounts and orders
 * 4. ANALYST: Business intelligence; read-only access to analytics and reports
 * 5. ADMIN: Full system access; can manage all resources, users, system configuration
 */
public enum UserRole {
  /**
   * CUSTOMER: Regular e-commerce customer.
   * Permissions: Browse catalog, add to cart, create orders, view own profile and orders
   */
  CUSTOMER("CUSTOMER"),

  /**
   * VENDOR: Seller/vendor managing storefront.
   * Permissions: Create/edit products, manage variants and inventory, view sales analytics
   */
  VENDOR("VENDOR"),

  /**
   * SUPPORT: Customer support representative.
   * Permissions: View all customer accounts, modify orders, manage support tickets, process refunds
   */
  SUPPORT("SUPPORT"),

  /**
   * ANALYST: Business intelligence analyst.
   * Permissions: Read-only access to analytics, reports, business metrics
   */
  ANALYST("ANALYST"),

  /**
   * ADMIN: System administrator with full access.
   * Permissions: Full system access - manage all modules, users, configuration, system settings
   */
  ADMIN("ADMIN");

  private final String value;

  UserRole(String value) {
    this.value = value;
  }

  /**
   * Get the string value of this role (used in JWT tokens and Spring Security).
   * 
   * @return the role name as used in JWT claims and @PreAuthorize expressions
   */
  public String value() {
    return value;
  }

  /**
   * Convert string role name to enum constant.
   * 
   * @param roleName role name (e.g., "ADMIN")
   * @return corresponding UserRole enum value
   * @throws IllegalArgumentException if roleName is not a valid role
   */
  public static UserRole fromValue(String roleName) {
    if (roleName == null) {
      throw new IllegalArgumentException("Role name cannot be null");
    }
    
    for (UserRole role : values()) {
      if (role.value.equals(roleName.toUpperCase())) {
        return role;
      }
    }
    
    throw new IllegalArgumentException(
        "Unknown role: '" + roleName + "'. Valid roles: CUSTOMER, VENDOR, SUPPORT, ANALYST, ADMIN"
    );
  }

  /**
   * Check if this role is administrative (ADMIN or above).
   * Used for simplified permission checks.
   * 
   * @return true if this is ADMIN role
   */
  public boolean isAdmin() {
    return this == ADMIN;
  }

  /**
   * Check if this role has support/admin capabilities.
   * Used for customer service operations.
   * 
   * @return true if role is SUPPORT or ADMIN
   */
  public boolean isSupportOrAdmin() {
    return this == SUPPORT || this == ADMIN;
  }
}
