package org.sirantar.recadero.users.events;

import java.util.List;

/**
 * Published when a user updates their profile.
 */
public record UserProfileUpdatedEvent(Long userId, List<String> fieldsChanged) {}
