package org.sirantar.recadero.users.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Validation rules for user-supplied account fields.
 */
@Service
public class UserValidationService {

  // Deliberately simple/pragmatic RFC 5322 subset: local@domain.tld
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,50}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
  private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
  private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");

  public void validateEmail(String email) {
    if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException("Email must be a valid address");
    }
  }

  public String normalizeEmail(String email) {
    return email.toLowerCase();
  }

  public void validatePassword(String password) {
    if (password == null
        || password.length() < 12
        || !UPPERCASE_PATTERN.matcher(password).matches()
        || !DIGIT_PATTERN.matcher(password).matches()
        || !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
      throw new IllegalArgumentException(
          "Password must be at least 12 characters and include an uppercase letter, a digit, and a special character");
    }
  }

  public void validateUsername(String username) {
    if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
      throw new IllegalArgumentException("Username must be 3-50 alphanumeric/underscore characters");
    }
  }

  public void validatePhone(String phone) {
    if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
      throw new IllegalArgumentException("Phone must be in E.164 format");
    }
  }
}
