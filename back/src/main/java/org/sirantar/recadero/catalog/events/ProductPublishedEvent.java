package org.sirantar.recadero.catalog.events;

/**
 * Published when a product transitions to published.
 */
public record ProductPublishedEvent(Long productId) {}
