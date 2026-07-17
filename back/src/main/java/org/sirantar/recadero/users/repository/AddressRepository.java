package org.sirantar.recadero.users.repository;

import java.util.List;
import java.util.Optional;
import org.sirantar.recadero.users.domain.Address;
import org.sirantar.recadero.users.domain.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for user addresses.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

  List<Address> findByUserId(Long userId);

  List<Address> findByUserIdAndType(Long userId, AddressType type);

  Optional<Address> findByUserIdAndIsPrimaryAndType(Long userId, boolean isPrimary, AddressType type);
}
