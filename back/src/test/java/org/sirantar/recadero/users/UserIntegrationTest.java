package org.sirantar.recadero.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.shared.security.JwtTokenProvider;
import org.sirantar.recadero.users.domain.Address;
import org.sirantar.recadero.users.domain.AddressType;
import org.sirantar.recadero.users.domain.EmailVerificationToken;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserProfile;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.events.UserEventPublisher;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.sirantar.recadero.users.repository.EmailVerificationTokenRepository;
import org.sirantar.recadero.users.repository.PasswordResetTokenRepository;
import org.sirantar.recadero.users.repository.UserProfileRepository;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.service.AddressService;
import org.sirantar.recadero.users.service.UserAuthenticationService;
import org.sirantar.recadero.users.service.UserProfileService;
import org.sirantar.recadero.users.service.UserRoleService;
import org.sirantar.recadero.users.service.UserValidationService;
import org.sirantar.recadero.users.service.dto.AddressRequest;
import org.sirantar.recadero.users.service.dto.LoginResponse;
import org.sirantar.recadero.users.service.dto.RegisterRequest;
import org.sirantar.recadero.users.service.dto.RegisterResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * End-to-end workflow tests for the Users module, exercising the service
 * layer across its full register/verify/login/address lifecycle with mocked
 * persistence (mirrors CatalogIntegrationTest's approach).
 */
@DisplayName("Users Module Integration Tests")
@ExtendWith(MockitoExtension.class)
class UserIntegrationTest {

  @Mock private UserRepository userRepository;
  @Mock private UserProfileRepository userProfileRepository;
  @Mock private AddressRepository addressRepository;
  @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private UserEventPublisher eventPublisher;
  @Mock private UserRoleService userRoleService;

  private UserProfileService userProfileService;
  private UserAuthenticationService authenticationService;
  private AddressService addressService;

  @BeforeEach
  void setUp() {
    userProfileService = new UserProfileService(userRepository, userProfileRepository, passwordEncoder, eventPublisher);
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
    addressService = new AddressService(addressRepository);
  }

  @Test
  @DisplayName("Should register, verify email, log in, and fetch profile")
  void registerVerifyLoginProfileFlow() {
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("janedoe")).thenReturn(false);
    when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User u = invocation.getArgument(0);
      if (u.getId() == null) {
        u.setId(42L);
      }
      return u;
    });
    when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RegisterRequest registerRequest =
        new RegisterRequest("Jane@Example.com", "janedoe", "SecurePass123!", "Jane", "Doe");
    RegisterResponse registered = authenticationService.registerUser(registerRequest);

    assertThat(registered.userId()).isEqualTo(42L);
    assertThat(registered.emailVerified()).isFalse();

    // Verify email
    EmailVerificationToken verificationToken = new EmailVerificationToken();
    verificationToken.setUserId(42L);
    verificationToken.setToken("verify-token");
    verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));
    when(emailVerificationTokenRepository.findByToken("verify-token")).thenReturn(Optional.of(verificationToken));

    User persistedUser = new User();
    persistedUser.setId(42L);
    persistedUser.setEmail("jane@example.com");
    persistedUser.setUsername("janedoe");
    persistedUser.setPasswordHash("hashed");
    persistedUser.setStatus(UserStatus.ACTIVE);
    persistedUser.setEmailVerified(false);
    persistedUser.setCreatedAt(LocalDateTime.now());
    when(userRepository.findById(42L)).thenReturn(Optional.of(persistedUser));

    Long verifiedUserId = authenticationService.verifyEmail("verify-token");
    assertThat(verifiedUserId).isEqualTo(42L);
    assertThat(persistedUser.getEmailVerified()).isTrue();

    // Login
    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(persistedUser));
    when(passwordEncoder.matches("SecurePass123!", "hashed")).thenReturn(true);
    when(userRoleService.getRoleNames(42L)).thenReturn(List.of("CUSTOMER"));
    when(jwtTokenProvider.generateAccessToken("42", "jane@example.com", List.of("CUSTOMER")))
        .thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken("42")).thenReturn("refresh-token");
    when(jwtTokenProvider.getAccessTokenExpiry()).thenReturn(900);
    when(userProfileRepository.findByUserId(42L)).thenReturn(Optional.of(profileFor(42L)));

    LoginResponse login = authenticationService.login("jane@example.com", "SecurePass123!");
    assertThat(login.accessToken()).isEqualTo("access-token");
    assertThat(login.user().userId()).isEqualTo(42L);

    // Get profile
    var profile = userProfileService.getProfile(42L);
    assertThat(profile.user().email()).isEqualTo("jane@example.com");
    assertThat(profile.profile().firstName()).isEqualTo("Jane");
  }

  @Test
  @DisplayName("Should add two addresses and set the second as primary")
  void addAddressesThenSetPrimaryFlow() {
    when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
      Address a = invocation.getArgument(0);
      if (a.getId() == null) {
        a.setId((long) (Math.random() * 1000) + 1);
      }
      return a;
    });

    AddressRequest first = new AddressRequest(
        "SHIPPING", "123 Main St", null, "Springfield", "IL", "62701", "US", true);
    var firstResponse = addressService.createAddress(7L, first);
    assertThat(firstResponse.isPrimary()).isTrue();

    Address firstEntity = new Address();
    firstEntity.setId(firstResponse.addressId());
    firstEntity.setUserId(7L);
    firstEntity.setType(AddressType.SHIPPING);
    firstEntity.setIsPrimary(true);
    when(addressRepository.findByUserIdAndIsPrimaryAndType(7L, true, AddressType.SHIPPING))
        .thenReturn(Optional.of(firstEntity));

    AddressRequest second = new AddressRequest(
        "SHIPPING", "456 Oak Ave", null, "Chicago", "IL", "60601", "US", false);
    var secondResponse = addressService.createAddress(7L, second);
    assertThat(secondResponse.isPrimary()).isFalse();

    Address secondEntity = new Address();
    secondEntity.setId(secondResponse.addressId());
    secondEntity.setUserId(7L);
    secondEntity.setType(AddressType.SHIPPING);
    secondEntity.setIsPrimary(false);
    when(addressRepository.findById(secondEntity.getId())).thenReturn(Optional.of(secondEntity));

    addressService.setPrimaryAddress(7L, secondEntity.getId());

    assertThat(firstEntity.getIsPrimary()).isFalse();
    assertThat(secondEntity.getIsPrimary()).isTrue();
  }

  private UserProfile profileFor(Long userId) {
    UserProfile profile = new UserProfile();
    profile.setUserId(userId);
    profile.setFirstName("Jane");
    profile.setLastName("Doe");
    profile.setPreferredLocale("en");
    profile.setPreferredCurrency("USD");
    profile.setNewsletterSubscribed(false);
    return profile;
  }
}
