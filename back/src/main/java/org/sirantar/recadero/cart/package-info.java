@org.springframework.modulith.ApplicationModule(
    displayName = "Cart",
    allowedDependencies = {
        // "cart::service" is a self-reference: CouponValidator's own nested
        // CouponValidationResult record trips Modulith's cross-named-interface
        // check even within the same module without it.
        "cart::service",
        "shared::dto", "shared::exception", "shared::security",
        "catalog::repository", "catalog::domain", "catalog::service"})
package org.sirantar.recadero.cart;
