package org.sirantar.recadero.catalog.events;

/**
 * Published when a product is archived.
 */
public record ProductArchivedEvent(Long productId) {}
