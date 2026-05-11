# Role-Based Access Control (RBAC) Implementation Guide

**Module**: Shared Security Infrastructure  
**Task**: T017 - Add role-based access control annotations (@PreAuthorize, @PostAuthorize)  
**Date**: 2026-05-11  
**Status**: ✅ Complete

---

## Overview

This document describes the comprehensive role-based access control (RBAC) infrastructure implemented for the Recadero backend. The system uses Spring Security's method-level security with custom SpEL expressions, annotations, and utilities to control access to endpoints and business logic.

## User Roles

Five roles are defined in the `UserRole` enum:

| Role | Permission Level | Use Cases |
|------|------------------|-----------|
| **CUSTOMER** | Lowest | Browse catalog, create orders, access own profile/history |
| **VENDOR** | Medium | Manage products, variants, inventory; view sales analytics |
| **SUPPORT** | Medium | Customer service, modify orders, process refunds |
| **ANALYST** | Medium-High | Read-only analytics and business metrics |
| **ADMIN** | Highest | Full system access, user management, configuration |

---

## Components

### 1. UserRole Enum
**File**: `shared/security/UserRole.java`

Enumeration of available user roles with helper methods.

```java
// Check if role is admin
userRole.isAdmin();  // true only for ADMIN

// Check if role has support capabilities
userRole.isSupportOrAdmin();  // true for SUPPORT and ADMIN

// Convert string to enum
UserRole role = UserRole.fromValue("ADMIN");
```

### 2. PermissionConstants
**File**: `shared/security/PermissionConstants.java`

Pre-defined security expressions for common authorization patterns.

```java
// Role-based constants
PermissionConstants.IS_ADMIN  // "hasRole('ADMIN')"
PermissionConstants.IS_CUSTOMER  // "hasRole('CUSTOMER')"
PermissionConstants.HAS_ADMIN_OR_SUPPORT_ROLE  // "hasAnyRole('ADMIN', 'SUPPORT')"

// General access constants
PermissionConstants.IS_AUTHENTICATED  // "isAuthenticated()"
PermissionConstants.IS_ANONYMOUS  // "permitAll()"

// Data ownership patterns
PermissionConstants.OWN_RESOURCE_ONLY_PREFIX  // "hasRole('CUSTOMER') and #"
PermissionConstants.OWN_RESOURCE_OR_ADMIN  // "or hasRole('ADMIN')"
```

### 3. SecurityExpressionRoot (SpEL Functions)
**File**: `shared/security/SecurityExpressionRoot.java`

Custom Spring Security SpEL functions available via `securityExpressions` bean in @PreAuthorize/@PostAuthorize.

```java
// Available methods (via securityExpressions in SpEL):
securityExpressions.isAdmin()  // Current user is ADMIN
securityExpressions.isCustomerOnly()  // Current user is CUSTOMER (only)
securityExpressions.isAdminOrSupport()  // Current user is ADMIN or SUPPORT
securityExpressions.isAdminOrVendor()  // Current user is ADMIN or VENDOR
securityExpressions.hasAnyRole('ADMIN', 'VENDOR')  // Check multiple roles
securityExpressions.hasAllRoles('ADMIN', 'ANALYST')  // All roles required
securityExpressions.isOwner(#userId)  // Current user owns resource (or is admin)
securityExpressions.isOwnerStrict(#userId)  // Strict ownership (no admin override)
securityExpressions.isCurrentUser(#userId)  // Exact user ID match
securityExpressions.isAdminOrOwner(#userId)  // Admin can view any, customers view own
securityExpressions.getCurrentUserId()  // Get UUID of current user
```

### 4. Custom Annotations
**Files**: 
- `shared/security/AdminOnly.java`
- `shared/security/CustomerOnly.java`
- `shared/security/Authenticated.java`

Convenient annotations for common permission patterns.

```java
@AdminOnly  // Equivalent to @PreAuthorize(PermissionConstants.IS_ADMIN)
public void deleteProduct(UUID id) { ... }

@CustomerOnly  // Equivalent to @PreAuthorize(PermissionConstants.IS_CUSTOMER)
public void addToCart(UUID productId) { ... }

@Authenticated  // Equivalent to @PreAuthorize(PermissionConstants.IS_AUTHENTICATED)
public UserResponse getProfile() { ... }
```

---

## Usage Patterns

### Pattern 1: Admin-Only Operations

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  // Option A: Using annotation
  @AdminOnly
  @PostMapping("/categories")
  public CategoryResponse createCategory(@RequestBody CategoryCreateRequest request) {
    return categoryService.create(request);
  }

  // Option B: Using PermissionConstants
  @PreAuthorize(PermissionConstants.IS_ADMIN)
  @DeleteMapping("/products/{id}")
  public void deleteProduct(@PathVariable UUID id) {
    productService.delete(id);
  }

  // Option C: Using SpEL
  @PreAuthorize("securityExpressions.isAdmin()")
  @PutMapping("/system/config")
  public void updateSystemConfig(@RequestBody ConfigUpdate config) {
    configService.update(config);
  }
}
```

### Pattern 2: Authenticated Users Only

```java
@RestController
@RequestMapping("/api/v1")
public class UserController {

  @Authenticated
  @GetMapping("/profile")
  public UserResponse getProfile(@AuthenticationPrincipal SecurityUser user) {
    return userService.getUser(UUID.fromString(user.getUserId()));
  }

  // Alternative: Using PermissionConstants
  @PreAuthorize(PermissionConstants.IS_AUTHENTICATED)
  @PutMapping("/profile")
  public UserResponse updateProfile(
      @AuthenticationPrincipal SecurityUser user,
      @RequestBody UpdateProfileRequest request
  ) {
    return userService.update(UUID.fromString(user.getUserId()), request);
  }
}
```

### Pattern 3: Owner-Only Access (with Admin Override)

```java
@RestController
@RequestMapping("/api/v1")
public class OrderController {

  // Admin can view any order; customers view only their own
  @PostAuthorize("securityExpressions.isAdminOrOwner(returnObject.userId)")
  @GetMapping("/orders/{id}")
  public OrderResponse getOrder(@PathVariable UUID id) {
    return orderService.get(id);
  }

  // Alternative: More explicit with SpEL
  @PreAuthorize("securityExpressions.isAdminOrOwner(#userId)")
  @GetMapping("/users/{userId}/orders")
  public List<OrderResponse> getUserOrders(@PathVariable UUID userId) {
    return orderService.getUserOrders(userId);
  }
}
```

### Pattern 4: Strict Ownership (No Admin Override)

```java
@RestController
@RequestMapping("/api/v1")
public class AccountController {

  // Even admins cannot access other users' account settings
  @PreAuthorize("securityExpressions.isOwnerStrict(#userId)")
  @PutMapping("/users/{userId}/account-settings")
  public void updateAccountSettings(
      @PathVariable UUID userId,
      @RequestBody AccountSettingsUpdate request
  ) {
    accountService.updateSettings(userId, request);
  }
}
```

### Pattern 5: Multi-Role Checks

```java
@RestController
@RequestMapping("/api/v1")
public class ServiceController {

  // Multiple roles allowed (admin or support)
  @PreAuthorize(PermissionConstants.HAS_ADMIN_OR_SUPPORT_ROLE)
  @GetMapping("/support/users/{id}")
  public UserResponse getSupportUserInfo(@PathVariable UUID id) {
    return userService.get(id);
  }

  // Using SpEL for more roles
  @PreAuthorize("securityExpressions.hasAnyRole('ADMIN', 'VENDOR', 'SUPPORT')")
  @GetMapping("/dashboard")
  public DashboardResponse getDashboard() {
    return dashboardService.get();
  }
}
```

### Pattern 6: Data-Level Authorization (@PostAuthorize)

```java
@RestController
@RequestMapping("/api/v1")
public class CartController {

  // Return value is checked after method execution
  @PostAuthorize("securityExpressions.isOwner(returnObject.userId) or hasRole('ADMIN')")
  @GetMapping("/carts/{cartId}")
  public CartResponse getCart(@PathVariable UUID cartId) {
    return cartService.get(cartId);
  }

  // Combining pre and post authorization
  @PreAuthorize(PermissionConstants.IS_AUTHENTICATED)
  @PostAuthorize("securityExpressions.isOwner(returnObject.createdBy)")
  @GetMapping("/invoices/{invoiceId}")
  public InvoiceResponse getInvoice(@PathVariable UUID invoiceId) {
    return invoiceService.get(invoiceId);
  }
}
```

### Pattern 7: Service-Level Authorization

```java
@Service
public class ProductService {

  // Protect business logic, not just endpoints
  @PreAuthorize(PermissionConstants.HAS_ADMIN_OR_VENDOR_ROLE)
  public ProductResponse publishProduct(UUID productId) {
    // Only vendors and admins can publish
    return publishProductInternal(productId);
  }

  @PreAuthorize("securityExpressions.isOwner(#createdBy)")
  public void deleteProduct(UUID productId, UUID createdBy) {
    // Vendor can delete own products; admins can delete any
    deleteProductInternal(productId);
  }

  // Custom complex logic
  @PreAuthorize("securityExpressions.hasAllRoles('ADMIN', 'ANALYST')")
  public AnalyticsReport generateAdvancedReport() {
    // Only users with both ADMIN and ANALYST role
    return reportService.generateAdvanced();
  }
}
```

---

## SecurityUser Principal

Access the current authenticated user in controllers and services:

```java
// In controller methods
@GetMapping("/me")
public UserResponse getProfile(@AuthenticationPrincipal SecurityUser user) {
  // user has all authentication info
  user.getUserId();  // UUID as String
  user.getEmail();  // Email address
  user.getRoles();  // List of role names
  user.hasRole("ADMIN");  // Check if user has role
  user.hasAnyRole("ADMIN", "VENDOR");  // Check multiple roles
}

// In service methods (using SecurityContextHolder)
@Service
public class UserService {
  public void updateMyProfile(UpdateProfileRequest request) {
    SecurityUser user = SecurityUser.getCurrentUser();
    if (user == null) {
      throw new UnauthorizedException("User not authenticated");
    }
    
    UUID userId = UUID.fromString(user.getUserId());
    updateUser(userId, request);
  }
}
```

---

## JWT Token Format

Roles are encoded in JWT tokens and extracted by Spring Security:

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "roles": ["CUSTOMER"],
  "iat": 1715425200,
  "exp": 1715428800,
  "aud": "recadero-api"
}
```

The `roles` claim contains a list of role names that Spring Security converts to `GrantedAuthority` objects.

---

## Configuration

**SecurityConfig.java** has the following key settings:

```java
@EnableMethodSecurity(
    prePostEnabled = true,      // Enable @PreAuthorize and @PostAuthorize
    securedEnabled = true,       // Enable @Secured({"ROLE_ADMIN"})
    jsr250Enabled = true         // Enable @RolesAllowed, @DenyAll, @PermitAll
)
```

**Custom Expression Handler**: 
- Registers `SecurityExpressionRoot` bean
- Makes custom SpEL functions available via `securityExpressions`
- Sets role prefix to empty (JWT uses plain role names, not "ROLE_" prefix)

---

## Best Practices

1. **Use Annotations for Simple Cases**: `@AdminOnly`, `@CustomerOnly` are cleaner to read
2. **Use PermissionConstants**: Avoid magic strings; import and use predefined constants
3. **Use Custom SpEL Functions**: `securityExpressions.isOwner(#userId)` is more maintainable than inline expressions
4. **Protect Services, Not Just Controllers**: Use @PreAuthorize on service methods for defense in depth
5. **Use @PostAuthorize for Data-Level Security**: Check return values to ensure data ownership
6. **Document Complex Expressions**: Add comments explaining non-obvious SpEL expressions
7. **Test Authorization**: Create integration tests for each security rule (see T032)
8. **Prefer Ownership Over Role**: When possible, use `isOwner()` instead of `isCustomer()` for multi-tenant safety

---

## Common Mistakes to Avoid

❌ **Don't use string literals**:
```java
@PreAuthorize("hasRole('ADMIN')")  // Anti-pattern
```

✅ **Do use constants**:
```java
@PreAuthorize(PermissionConstants.IS_ADMIN)  // Good
```

---

❌ **Don't assume role names include "ROLE_" prefix**:
```java
@PreAuthorize("hasRole('ROLE_ADMIN')")  // Wrong for JWT
```

✅ **Do use plain role names**:
```java
@PreAuthorize("hasRole('ADMIN')")  // Correct for JWT
```

---

❌ **Don't allow admin override for sensitive operations**:
```java
@PreAuthorize("securityExpressions.isOwner(#userId) or hasRole('ADMIN')")
public void deleteUserAccount(UUID userId) { ... }  // Dangerous!
```

✅ **Do use strict ownership when needed**:
```java
@PreAuthorize("securityExpressions.isOwnerStrict(#userId)")
public void deleteUserAccount(UUID userId) { ... }  // Safe
```

---

## Related Files

- **SecurityConfig.java**: Main Spring Security configuration
- **JwtAuthenticationConverter.java**: Converts JWT to Authentication
- **JwtTokenProvider.java**: Generates and validates JWT tokens
- **SecurityUser.java**: Custom UserDetails implementation
- **Test utilities**: `JwtTokenTestUtils.java` (created in T031)

---

## Next Steps

1. Use these RBAC patterns when implementing module controllers (Phases 3-8)
2. Write integration tests verifying authorization (T032)
3. Document any custom security logic specific to your module in its own README
