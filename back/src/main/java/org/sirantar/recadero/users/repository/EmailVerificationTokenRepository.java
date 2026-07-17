package org.sirantar.recadero.users.repository;

import java.util.Optional;
import org.sirantar.recadero.users.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for email verification tokens.
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

  Optional<EmailVerificationToken> findByToken(String token);
}
