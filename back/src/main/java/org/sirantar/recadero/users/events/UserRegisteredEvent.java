package org.sirantar.recadero.users.events;

/**
 * Published when a user completes registration.
 */
public record UserRegisteredEvent(Long userId, String email, String username) {}
