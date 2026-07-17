package org.sirantar.recadero.users.repository;

import java.util.Optional;
import org.sirantar.recadero.users.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for password reset tokens.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByToken(String token);
}
