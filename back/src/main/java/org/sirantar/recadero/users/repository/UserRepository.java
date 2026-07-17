package org.sirantar.recadero.users.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for user accounts.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  List<User> findByStatusOrderByCreatedAtDesc(UserStatus status);

  Page<User> findByStatus(UserStatus status, Pageable pageable);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
