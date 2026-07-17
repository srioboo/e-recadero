package org.sirantar.recadero.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.shared.exception.BusinessLogicException;
import org.sirantar.recadero.shared.security.JwtTokenProvider;
import org.sirantar.recadero.users.domain.EmailVerificationToken;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.events.UserEventPublisher;
import org.sirantar.recadero.users.repository.EmailVerificationTokenRepository;
import org.sirantar.recadero.users.repository.PasswordResetTokenRepository;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.service.dto.LoginResponse;
import org.sirantar.recadero.users.service.dto.RegisterRequest;
import org.sirantar.recadero.users.service.dto.RegisterResponse;
import org.sirantar.recadero.users.service.dto.UserProfileResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserProfileService userProfileService;
  @Mock private UserRoleService userRoleService;
  @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private UserEventPublisher eventPublisher;

  private UserAuthenticationService authenticationService;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Constructed manually (rather than via @InjectMocks) so the real
    // UserValidationService can be used alongside the mocked collaborators.
    authenticationService = new UserAuthenticationService(
        userRepository,
        userProfileService,
        userRoleService,
        new UserValidationService(),
        emailVerificationTokenRepository,
        passwordResetTokenRepository,
        passwordEncoder,
        jwtTokenProvider,
        redisTemplate,
        eventPublisher);

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("user@example.com");
    testUser.setUsername("johndoe");
    testUser.setPasswordHash("hashed-password");
    testUser.setStatus(UserStatus.ACTIVE);
    testUser.setEmailVerified(false);
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void registerUserSuccess() {
    RegisterRequest request =
        new RegisterRequest("New@Example.com", "newuser", "SecurePass123!", "New", "User");

    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User u = invocation.getArgument(0);
      u.setId(2L);
      return u;
    });

    RegisterResponse response = authenticationService.registerUser(request);

    assertThat(response.userId()).isEqualTo(2L);
    assertThat(response.email()).isEqualTo("new@example.com");
    assertThat(response.emailVerified()).isFalse();
    verify(userProfileService).createProfile(2L);
    verify(userRoleService).grantRole(eq(2L), eq("CUSTOMER"), anyString());
    verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
    verify(eventPublisher).publishRegistered(2L, "new@example.com", "newuser");
  }

  @Test
  void registerUserRejectsDuplicateEmail() {
    RegisterRequest request =
        new RegisterRequest("user@example.com", "newuser", "SecurePass123!", "New", "User");

    when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.registerUser(request))
        .isInstanceOf(BusinessLogicException.class)
        .hasMessageContaining("Email already registered");

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void loginSucceedsWithValidCredentials() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userRoleService.getRoleNames(1L)).thenReturn(List.of("CUSTOMER"));
    when(jwtTokenProvider.generateAccessToken(eq("1"), eq("user@example.com"), anyList()))
        .thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken("1")).thenReturn("refresh-token");
    when(jwtTokenProvider.getAccessTokenExpiry()).thenReturn(900);
    when(userProfileService.getProfile(1L)).thenReturn(new UserProfileResponse(
        new UserProfileResponse.UserSummary(1L, "user@example.com", "johndoe", "ACTIVE", false, LocalDateTime.now()),
        new UserProfileResponse.ProfileDetail("John", "Doe", null, null, null, "en", "USD", false)));

    LoginResponse response = authenticationService.login("user@example.com", "Password123!");

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.user().roles()).containsExactly("CUSTOMER");
  }

  @Test
  void loginRejectsInvalidPassword() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

    assertThatThrownBy(() -> authenticationService.login("user@example.com", "wrong"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void loginRejectsUnknownEmail() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authenticationService.login("missing@example.com", "whatever"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void refreshTokenGeneratesNewAccessToken() {
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
    when(redisTemplate.hasKey("auth:revoked-refresh-token:refresh-token")).thenReturn(false);
    when(jwtTokenProvider.extractSubject("refresh-token")).thenReturn("1");
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRoleService.getRoleNames(1L)).thenReturn(List.of("CUSTOMER"));
    when(jwtTokenProvider.generateAccessToken(eq("1"), eq("user@example.com"), anyList()))
        .thenReturn("new-access-token");
    when(jwtTokenProvider.getAccessTokenExpiry()).thenReturn(900);

    var response = authenticationService.refreshToken("refresh-token");

    assertThat(response.accessToken()).isEqualTo("new-access-token");
  }

  @Test
  void refreshTokenRejectsRevokedToken() {
    when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
    when(redisTemplate.hasKey("auth:revoked-refresh-token:refresh-token")).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.refreshToken("refresh-token"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void verifyEmailActivatesAccount() {
    EmailVerificationToken token = new EmailVerificationToken();
    token.setUserId(1L);
    token.setToken("verify-token");
    token.setExpiresAt(LocalDateTime.now().plusHours(1));

    when(emailVerificationTokenRepository.findByToken("verify-token")).thenReturn(Optional.of(token));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    Long userId = authenticationService.verifyEmail("verify-token");

    assertThat(userId).isEqualTo(1L);
    assertThat(testUser.getEmailVerified()).isTrue();
    verify(eventPublisher).publishEmailVerified(1L, "user@example.com");
  }

  @Test
  void verifyEmailRejectsExpiredToken() {
    EmailVerificationToken token = new EmailVerificationToken();
    token.setUserId(1L);
    token.setToken("expired-token");
    token.setExpiresAt(LocalDateTime.now().minusHours(1));

    when(emailVerificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> authenticationService.verifyEmail("expired-token"))
        .isInstanceOf(BusinessLogicException.class);
  }

  @Test
  void logoutRevokesRefreshTokenInRedis() {
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(jwtTokenProvider.getExpirationTimeInSeconds("refresh-token")).thenReturn(600L);

    authenticationService.logout("refresh-token");

    verify(valueOperations).set(
        eq("auth:revoked-refresh-token:refresh-token"),
        eq("revoked"),
        eq(600L),
        eq(java.util.concurrent.TimeUnit.SECONDS));
  }
}
