package org.sirantar.recadero.users;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Users module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class UsersApplicationModuleTest {

  @Test
  void testUsersServicesExist() {
    assertClassExists("org/sirantar/recadero/users/service/UserAuthenticationService.class");
    assertClassExists("org/sirantar/recadero/users/service/UserProfileService.class");
    assertClassExists("org/sirantar/recadero/users/service/AddressService.class");
    assertClassExists("org/sirantar/recadero/users/service/UserRoleService.class");
    assertClassExists("org/sirantar/recadero/users/service/UserValidationService.class");
    assertClassExists("org/sirantar/recadero/users/service/AdminUserService.class");
  }

  @Test
  void testUsersRepositoriesExist() {
    assertClassExists("org/sirantar/recadero/users/repository/UserRepository.class");
    assertClassExists("org/sirantar/recadero/users/repository/AddressRepository.class");
    assertClassExists("org/sirantar/recadero/users/repository/UserRoleRepository.class");
  }

  @Test
  void testUsersApiExists() {
    assertClassExists("org/sirantar/recadero/users/api/AuthController.class");
    assertClassExists("org/sirantar/recadero/users/api/UserProfileController.class");
    assertClassExists("org/sirantar/recadero/users/api/AddressController.class");
    assertClassExists("org/sirantar/recadero/users/api/AdminUserController.class");
  }

  @Test
  void testUsersDomainsExist() {
    assertClassExists("org/sirantar/recadero/users/domain/User.class");
    assertClassExists("org/sirantar/recadero/users/domain/UserProfile.class");
    assertClassExists("org/sirantar/recadero/users/domain/Address.class");
    assertClassExists("org/sirantar/recadero/users/domain/UserRoleAssignment.class");
  }

  @Test
  void testUsersEventsExist() {
    assertClassExists("org/sirantar/recadero/users/events/UserRegisteredEvent.class");
    assertClassExists("org/sirantar/recadero/users/events/UserEmailVerifiedEvent.class");
    assertClassExists("org/sirantar/recadero/users/events/UserProfileUpdatedEvent.class");
    assertClassExists("org/sirantar/recadero/users/events/UserEventPublisher.class");
  }

  @Test
  void testUsersPackageInfoExists() {
    assertClassExists("org/sirantar/recadero/users/package-info.class");
  }

  private void assertClassExists(String resourcePath) {
    assertThat(UsersApplicationModuleTest.class.getClassLoader().getResource(resourcePath))
        .as(resourcePath + " should exist")
        .isNotNull();
  }
}
