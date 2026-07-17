package org.sirantar.recadero.users.events;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for the Users module's domain events.
 */
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishRegistered(Long userId, String email, String username) {
    eventPublisher.publishEvent(new UserRegisteredEvent(userId, email, username));
  }

  public void publishEmailVerified(Long userId, String email) {
    eventPublisher.publishEvent(new UserEmailVerifiedEvent(userId, email));
  }

  public void publishProfileUpdated(Long userId, List<String> fieldsChanged) {
    eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId, fieldsChanged));
  }
}
