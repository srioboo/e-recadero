@org.springframework.modulith.ApplicationModule(
    displayName = "Orders",
    allowedDependencies = {
        "shared::dto", "shared::exception", "shared::security",
        "cart::events",
        "catalog::repository", "catalog::domain", "catalog::service",
        "users::repository", "users::domain"})
package org.sirantar.recadero.orders;
