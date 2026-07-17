package org.sirantar.recadero.users.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidationServiceTest {

  private final UserValidationService validationService = new UserValidationService();

  @ParameterizedTest
  @ValueSource(strings = {"user@example.com", "first.last+tag@sub.example.co"})
  void validateEmailAcceptsValidAddresses(String email) {
    assertThatCode(() -> validationService.validateEmail(email)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-an-email", "missing-domain@", "@missing-local.com", ""})
  void validateEmailRejectsInvalidAddresses(String email) {
    assertThatThrownBy(() -> validationService.validateEmail(email))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateEmailRejectsNull() {
    assertThatThrownBy(() -> validationService.validateEmail(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatePasswordAcceptsStrongPassword() {
    assertThatCode(() -> validationService.validatePassword("SecurePass123!"))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"short1!", "nouppercase123!", "NOLOWERCASEORDIGIT!!!!", "NoSpecialChars123"})
  void validatePasswordRejectsWeakPasswords(String password) {
    assertThatThrownBy(() -> validationService.validatePassword(password))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"johndoe", "john_doe_123", "abc"})
  void validateUsernameAcceptsValidNames(String username) {
    assertThatCode(() -> validationService.validateUsername(username)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"ab", "has spaces", "has-dash", ""})
  void validateUsernameRejectsInvalidNames(String username) {
    assertThatThrownBy(() -> validationService.validateUsername(username))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatePhoneAcceptsE164Format() {
    assertThatCode(() -> validationService.validatePhone("+12025550123")).doesNotThrowAnyException();
  }

  @Test
  void validatePhoneAcceptsNullOrBlank() {
    assertThatCode(() -> validationService.validatePhone(null)).doesNotThrowAnyException();
    assertThatCode(() -> validationService.validatePhone("")).doesNotThrowAnyException();
  }

  @Test
  void validatePhoneRejectsNonE164Format() {
    assertThatThrownBy(() -> validationService.validatePhone("555-0123"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void normalizeEmailLowercases() {
    assertThatCode(() -> validationService.normalizeEmail("User@Example.COM")).doesNotThrowAnyException();
    org.assertj.core.api.Assertions.assertThat(validationService.normalizeEmail("User@Example.COM"))
        .isEqualTo("user@example.com");
  }
}
