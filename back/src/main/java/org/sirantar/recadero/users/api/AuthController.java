package org.sirantar.recadero.users.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.security.Authenticated;
import org.sirantar.recadero.users.service.UserAuthenticationService;
import org.sirantar.recadero.users.service.dto.ForgotPasswordRequest;
import org.sirantar.recadero.users.service.dto.LoginRequest;
import org.sirantar.recadero.users.service.dto.LoginResponse;
import org.sirantar.recadero.users.service.dto.MessageResponse;
import org.sirantar.recadero.users.service.dto.RefreshTokenRequest;
import org.sirantar.recadero.users.service.dto.RefreshTokenResponse;
import org.sirantar.recadero.users.service.dto.RegisterRequest;
import org.sirantar.recadero.users.service.dto.RegisterResponse;
import org.sirantar.recadero.users.service.dto.ResetPasswordRequest;
import org.sirantar.recadero.users.service.dto.VerifyEmailRequest;
import org.sirantar.recadero.users.service.dto.VerifyEmailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints (registration, login, token lifecycle).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserAuthenticationService authenticationService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public RegisterResponse register(@RequestBody RegisterRequest request) {
    return authenticationService.registerUser(request);
  }

  @PostMapping("/verify-email")
  public VerifyEmailResponse verifyEmail(@RequestBody VerifyEmailRequest request) {
    Long userId = authenticationService.verifyEmail(request.token());
    return new VerifyEmailResponse(userId, "Email verified successfully");
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authenticationService.login(request.email(), request.password());
  }

  @PostMapping("/refresh-token")
  public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {
    return authenticationService.refreshToken(request.refreshToken());
  }

  @Authenticated
  @PostMapping("/logout")
  public MessageResponse logout(@RequestBody RefreshTokenRequest request) {
    authenticationService.logout(request.refreshToken());
    return new MessageResponse("Logged out successfully");
  }

  @PostMapping("/forgot-password")
  public MessageResponse forgotPassword(@RequestBody ForgotPasswordRequest request) {
    authenticationService.forgotPassword(request.email());
    return new MessageResponse("Password reset link sent to your email");
  }

  @PostMapping("/reset-password")
  public MessageResponse resetPassword(@RequestBody ResetPasswordRequest request) {
    authenticationService.resetPassword(request.token(), request.newPassword());
    return new MessageResponse("Password reset successfully");
  }
}
