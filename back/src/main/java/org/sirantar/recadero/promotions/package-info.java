@org.springframework.modulith.ApplicationModule(
    displayName = "Promotions",
    allowedDependencies = {
        "shared::dto", "shared::exception", "shared::security",
        "catalog::repository", "catalog::domain",
        "orders::events", "orders::repository",
        "cart::service"})
package org.sirantar.recadero.promotions;
