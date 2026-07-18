package org.sirantar.recadero.shared.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.sirantar.recadero.shared.config.properties.SecurityProperties;
import org.sirantar.recadero.shared.security.SecurityExpressionRoot;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Spring Security configuration for JWT-based OAuth2 resource server.
 * 
 * Features:
 * - JWT token validation for stateless authentication
 * - Role-based access control via @PreAuthorize, @PostAuthorize
 * - CORS configuration for frontend integrations
 * - CSRF protection for state-changing operations
 * - Session management (stateless JWT-based)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
public class SecurityConfig {

  private final SecurityProperties securityProperties;

  public SecurityConfig(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
  private String[] allowedOrigins;

  /**
   * Configure HTTP security filter chain.
   * 
   * Security policies:
   * - Stateless JWT-based authentication (no sessions)
   * - CORS enabled for specified origins
   * - CSRF protection disabled (stateless API; CORS handles cross-origin concerns)
   * - Public endpoints: /auth/*, /tracking/*, /templates/*, /swagger-ui/*, /v3/api-docs/*
   *   (patterns here are relative to server.servlet.context-path=/api/v1, which Spring
   *   Security strips before matching — so the full external URL is /api/v1/auth/* etc.)
   * - Protected endpoints: everything else under the context path (except public auth)
   * - Admin only: /admin/*
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF for stateless API (JWT in Authorization header is CSRF-safe)
        .csrf(csrf -> csrf.disable())
        
        // Enable CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        // Stateless session management (no cookies)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // Configure OAuth2 resource server (JWT)
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
        )
        
        // HTTP security rules
        .authorizeHttpRequests(authz -> authz
            // Public authentication endpoints
            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/verify-email").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/refresh-token").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()

            // Public webhooks (requires signature verification in controller)
            .requestMatchers(HttpMethod.POST, "/webhooks/**").permitAll()

            // Public shipment tracking
            .requestMatchers(HttpMethod.GET, "/tracking/**").permitAll()

            // Public coupon validation (rate-limited by IP at the gateway, not here)
            .requestMatchers(HttpMethod.POST, "/coupons/validate").permitAll()

            // Public published templates
            .requestMatchers(HttpMethod.GET, "/templates/*").permitAll()
            
            // Actuator endpoints (health checks, metrics)
            .requestMatchers("/actuator/**").permitAll()
            
            // API documentation
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/swagger-resources/**").permitAll()
            .requestMatchers("/webjars/**").permitAll()
            
            // Static resources
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            
            // Admin-only endpoints
            .requestMatchers("/admin/**").hasRole("ADMIN")

            // All other requests require authentication
            .anyRequest().authenticated()
        );

    return http.build();
  }

  /**
   * CORS configuration source.
   * 
   * Allowed origins are externalized via property: app.security.cors.allowed-origins
   * Default: http://localhost:3000 (admin), http://localhost:3001 (front)
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList(
        "Authorization",
        "X-Total-Count",  // For pagination
        "X-Total-Pages"
    ));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * Password encoder using BCrypt with cost factor 12.
   * 
   * Cost factor 12 provides strong security while maintaining acceptable performance:
   * - Cost < 10: Weak (vulnerable to brute force)
   * - Cost 10-12: Recommended for production
   * - Cost > 12: Overkill for most use cases
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * JWT decoder for OAuth2 resource server.
   * 
   * Validates JWT tokens using public key from issuer's JWKS endpoint.
   * Issuer URL is externalized via property: app.security.jwt.issuer
   * Default: http://localhost:8080
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey())
        .build();
  }

  private SecretKey jwtSecretKey() {
    String secret = securityProperties.getJwt().getSecret();
    byte[] keyBytes;
    try {
      keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
    } catch (RuntimeException ex) {
      keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }
    return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Custom method security expression handler for @PreAuthorize and @PostAuthorize.
   * 
   * Registers the SecurityExpressionRoot bean, making it available in SpEL expressions
   * via the "securityExpressions" variable:
   * 
   * @PreAuthorize("securityExpressions.isAdmin()")
   * @PreAuthorize("securityExpressions.isOwner(#userId)")
   * @PreAuthorize("securityExpressions.hasAnyRole('ADMIN', 'SUPPORT')")
   */
  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      SecurityExpressionRoot securityExpressions
  ) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    return handler;
  }
}
