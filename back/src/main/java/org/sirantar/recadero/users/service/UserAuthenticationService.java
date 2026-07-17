package org.sirantar.recadero.users.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.shared.security.JwtTokenProvider;
import org.sirantar.recadero.shared.security.UserRole;
import org.sirantar.recadero.users.domain.EmailVerificationToken;
import org.sirantar.recadero.users.domain.PasswordResetToken;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.events.UserEventPublisher;
import org.sirantar.recadero.users.repository.EmailVerificationTokenRepository;
import org.sirantar.recadero.users.repository.PasswordResetTokenRepository;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.service.dto.LoginResponse;
import org.sirantar.recadero.users.service.dto.RefreshTokenResponse;
import org.sirantar.recadero.users.service.dto.RegisterRequest;
import org.sirantar.recadero.users.service.dto.RegisterResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration, email verification, login, token refresh, and logout.
 */
@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

  private static final String REVOKED_TOKEN_PREFIX = "auth:revoked-refresh-token:";
  private static final int EMAIL_VERIFICATION_TOKEN_TTL_HOURS = 24;
  private static final int PASSWORD_RESET_TOKEN_TTL_HOURS = 1;

  private final UserRepository userRepository;
  private final UserProfileService userProfileService;
  private final UserRoleService userRoleService;
  private final UserValidationService userValidationService;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final StringRedisTemplate redisTemplate;
  private final UserEventPublisher eventPublisher;

  @Transactional
  public RegisterResponse registerUser(RegisterRequest request) {
    String email = userValidationService.normalizeEmail(request.email());
    userValidationService.validateEmail(email);
    userValidationService.validateUsername(request.username());
    userValidationService.validatePassword(request.password());

    if (userRepository.existsByEmail(email)) {
      throw new BusinessLogicException("USER_ALREADY_EXISTS", "Email already registered");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new BusinessLogicException("USER_ALREADY_EXISTS", "Username already taken");
    }

    User user = new User();
    user.setEmail(email);
    user.setUsername(request.username());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setStatus(UserStatus.ACTIVE);
    user.setEmailVerified(false);
    LocalDateTime now = LocalDateTime.now();
    user.setCreatedAt(now);
    user.setUpdatedAt(now);
    User saved = userRepository.save(user);

    userProfileService.createProfile(saved.getId());
    userRoleService.grantRole(saved.getId(), UserRole.CUSTOMER.value(), "system:self-registration");

    issueEmailVerificationToken(saved);

    eventPublisher.publishRegistered(saved.getId(), saved.getEmail(), saved.getUsername());

    return new RegisterResponse(
        saved.getId(), saved.getEmail(), saved.getUsername(), false,
        "Registration successful. Please verify your email.");
  }

  @Transactional
  public Long verifyEmail(String token) {
    EmailVerificationToken verification = emailVerificationTokenRepository.findByToken(token)
        .orElseThrow(() -> new BusinessLogicException("EXPIRED_TOKEN", "Verification token is invalid"));

    if (verification.getUsedAt() != null || verification.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessLogicException("EXPIRED_TOKEN", "Verification token has expired");
    }

    User user = userRepository.findById(verification.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setEmailVerified(true);
    user.setEmailVerifiedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);

    verification.setUsedAt(LocalDateTime.now());
    emailVerificationTokenRepository.save(verification);

    eventPublisher.publishEmailVerified(user.getId(), user.getEmail());
    return user.getId();
  }

  @Transactional
  public LoginResponse login(String email, String password) {
    User user = userRepository.findByEmail(userValidationService.normalizeEmail(email))
        .orElseThrow(() -> new BadCredentialsException("Email or password incorrect"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new BadCredentialsException("Email or password incorrect");
    }
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new BadCredentialsException("Account is not active");
    }

    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    List<String> roles = userRoleService.getRoleNames(user.getId());
    String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getEmail(), roles);
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

    var profileResponse = userProfileService.getProfile(user.getId());
    LoginResponse.LoginProfile profile = new LoginResponse.LoginProfile(
        profileResponse.profile().firstName(),
        profileResponse.profile().lastName(),
        profileResponse.profile().avatarUrl(),
        profileResponse.profile().preferredLocale());

    LoginResponse.LoginUser loginUser =
        new LoginResponse.LoginUser(user.getId(), user.getEmail(), user.getUsername(), roles, profile);

    return new LoginResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiry(), loginUser);
  }

  public RefreshTokenResponse refreshToken(String refreshToken) {
    if (!jwtTokenProvider.validateToken(refreshToken) || isRevoked(refreshToken)) {
      throw new BadCredentialsException("Refresh token is invalid or expired");
    }

    String userId = jwtTokenProvider.extractSubject(refreshToken);
    User user = userRepository.findById(Long.valueOf(userId))
        .orElseThrow(() -> new BadCredentialsException("Refresh token is invalid or expired"));

    List<String> roles = userRoleService.getRoleNames(user.getId());
    String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail(), roles);
    return new RefreshTokenResponse(accessToken, jwtTokenProvider.getAccessTokenExpiry());
  }

  public void logout(String refreshToken) {
    long ttlSeconds = jwtTokenProvider.getExpirationTimeInSeconds(refreshToken);
    if (ttlSeconds > 0) {
      redisTemplate.opsForValue().set(REVOKED_TOKEN_PREFIX + refreshToken, "revoked", ttlSeconds, TimeUnit.SECONDS);
    }
  }

  private boolean isRevoked(String refreshToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_TOKEN_PREFIX + refreshToken));
  }

  @Transactional
  public void forgotPassword(String email) {
    userRepository.findByEmail(userValidationService.normalizeEmail(email)).ifPresent(user -> {
      PasswordResetToken resetToken = new PasswordResetToken();
      resetToken.setUserId(user.getId());
      resetToken.setToken(UUID.randomUUID().toString());
      LocalDateTime now = LocalDateTime.now();
      resetToken.setCreatedAt(now);
      resetToken.setExpiresAt(now.plus(PASSWORD_RESET_TOKEN_TTL_HOURS, ChronoUnit.HOURS));
      passwordResetTokenRepository.save(resetToken);
      // Actual email delivery is out of scope: a future Email Service module
      // is expected to send it via the domain event stream.
    });
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    userValidationService.validatePassword(newPassword);

    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
        .orElseThrow(() -> new BusinessLogicException("EXPIRED_TOKEN", "Reset token is invalid"));

    if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessLogicException("EXPIRED_TOKEN", "Reset token has expired");
    }

    User user = userRepository.findById(resetToken.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setPasswordChangedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);

    resetToken.setUsedAt(LocalDateTime.now());
    passwordResetTokenRepository.save(resetToken);
  }

  private void issueEmailVerificationToken(User user) {
    EmailVerificationToken verification = new EmailVerificationToken();
    verification.setUserId(user.getId());
    verification.setToken(UUID.randomUUID().toString());
    LocalDateTime now = LocalDateTime.now();
    verification.setCreatedAt(now);
    verification.setExpiresAt(now.plus(EMAIL_VERIFICATION_TOKEN_TTL_HOURS, ChronoUnit.HOURS));
    emailVerificationTokenRepository.save(verification);
  }
}
