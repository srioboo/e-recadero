package org.sirantar.recadero.shared.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for admin-only operations.
 * 
 * Restricts access to users with the ADMIN role.
 * Used for sensitive operations like:
 * - Category, product, and inventory management
 * - User management and account suspension
 * - System configuration and settings
 * - Promotion rule creation and modification
 * - Template management
 * 
 * Usage:
 * @AdminOnly
 * @PostMapping("/categories")
 * public void createCategory(@RequestBody CategoryCreateRequest request) { ... }
 * 
 * Equivalent to:
 * @PreAuthorize(PermissionConstants.IS_ADMIN)
 * 
 * The annotation is composable and can be combined with custom SpEL expressions via @PreAuthorize
 * if more fine-grained control is needed.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize(PermissionConstants.IS_ADMIN)
public @interface AdminOnly {
}
