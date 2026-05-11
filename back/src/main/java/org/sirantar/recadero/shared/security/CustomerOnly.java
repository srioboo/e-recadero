package org.sirantar.recadero.shared.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for customer-only operations.
 * 
 * Restricts access to users with the CUSTOMER role.
 * Used for customer-specific operations like:
 * - Viewing own profile and order history
 * - Adding items to cart
 * - Creating orders
 * - Updating own email or password
 * 
 * Note: For operations where customers access their own data but admins can access any data,
 * use @PreAuthorize with custom SpEL: @PreAuthorize("hasRole('CUSTOMER') and #userId == authentication.principal.userId or hasRole('ADMIN')")
 * 
 * Usage:
 * @CustomerOnly
 * @PostMapping("/cart/items")
 * public void addToCart(@RequestBody CartItemRequest request) { ... }
 * 
 * Equivalent to:
 * @PreAuthorize(PermissionConstants.IS_CUSTOMER)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize(PermissionConstants.IS_CUSTOMER)
public @interface CustomerOnly {
}
