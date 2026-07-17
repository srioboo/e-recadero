package org.sirantar.recadero.users.events;

/**
 * Published when a user verifies their email address.
 */
public record UserEmailVerifiedEvent(Long userId, String email) {}
