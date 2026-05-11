package org.sirantar.recadero.shared.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for authenticated user access.
 * 
 * Allows access to any authenticated user regardless of role.
 * Used for user-facing operations like:
 * - Viewing public product catalog
 * - Searching for products
 * - Viewing own profile
 * - Accessing own order history
 * - Managing own account settings
 * 
 * Usage:
 * @Authenticated
 * @GetMapping("/profile")
 * public UserResponse getProfile() { ... }
 * 
 * Equivalent to:
 * @PreAuthorize(PermissionConstants.IS_AUTHENTICATED)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize(PermissionConstants.IS_AUTHENTICATED)
public @interface Authenticated {
}
