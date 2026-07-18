@org.springframework.modulith.ApplicationModule(
    displayName = "Templates",
    allowedDependencies = {
        "shared::dto", "shared::exception", "shared::security",
        "catalog::repository", "catalog::domain"})
package org.sirantar.recadero.templates;
