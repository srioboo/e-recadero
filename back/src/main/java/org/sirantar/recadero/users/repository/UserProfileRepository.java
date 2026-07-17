package org.sirantar.recadero.users.repository;

import java.util.Optional;
import org.sirantar.recadero.users.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for user profile records.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  Optional<UserProfile> findByUserId(Long userId);
}
