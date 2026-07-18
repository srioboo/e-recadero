package org.sirantar.recadero.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

/**
 * OpenAPI 3.0 (Swagger) configuration for API documentation and interactive UI.
 *
 * Provides:
 * - Auto-generated OpenAPI 3.0 specification at /v3/api-docs
 * - Swagger UI at /swagger-ui.html
 * - JWT Bearer token security scheme
 * - API grouping by module (catalog, users, cart, orders, promotions, templates)
 * - Server configuration for dev/prod environments
 *
 * @OpenAPIDefinition: top-level API metadata (title, version, contact)
 * @SecurityScheme: JWT Bearer token configuration for interactive testing
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "E-Recadero API",
        version = "1.0.0",
        description = "Modular e-commerce backend with Spring Boot 3.x and Modulith architecture. " +
                      "Manages product catalogs, user accounts, shopping carts, orders, promotions, and dynamic templates.",
        contact = @Contact(
            name = "Recadero Development Team",
            url = "https://github.com/sirantar/e-recadero"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local development server"
        ),
        @Server(
            url = "https://api-staging.recadero.com",
            description = "Staging environment"
        ),
        @Server(
            url = "https://api.recadero.com",
            description = "Production environment"
        )
    }
)
@SecurityScheme(
    name = "Bearer",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT Bearer token obtained from /api/v1/auth/login endpoint. " +
                  "Include as Authorization header: Bearer <token>"
)
public class OpenApiConfig {

  @Value("${app.name:E-Recadero}")
  private String appName;

  @Value("${app.version:1.0.0}")
  private String appVersion;

  /**
   * Catalog module API group.
   * Includes: products, categories, inventory, search endpoints
   */
  @Bean
  public GroupedOpenApi catalogApi() {
    return GroupedOpenApi.builder()
        .group("catalog")
        .displayName("Catalog Module")
        .pathsToMatch("/categories/**", "/products/**", "/inventory/**")
        .build();
  }

  /**
   * Users module API group.
   * Includes: authentication, user profiles, addresses, role management
   */
  @Bean
  public GroupedOpenApi usersApi() {
    return GroupedOpenApi.builder()
        .group("users")
        .displayName("Users & Authentication")
        .pathsToMatch("/auth/**", "/users/**", "/admin/users/**")
        .build();
  }

  /**
   * Shopping Cart module API group.
   * Includes: cart operations, item management, coupons, checkout
   */
  @Bean
  public GroupedOpenApi cartApi() {
    return GroupedOpenApi.builder()
        .group("cart")
        .displayName("Shopping Cart")
        .pathsToMatch("/cart/**")
        .build();
  }

  /**
   * Orders module API group.
   * Includes: order management, shipment tracking, returns, refunds
   */
  @Bean
  public GroupedOpenApi ordersApi() {
    return GroupedOpenApi.builder()
        .group("orders")
        .displayName("Orders & Fulfillment")
        .pathsToMatch("/orders/**", "/tracking/**", "/admin/orders/**")
        .build();
  }

  /**
   * Promotions module API group.
   * Includes: promotions, coupon codes, discount calculations
   */
  @Bean
  public GroupedOpenApi promotionsApi() {
    return GroupedOpenApi.builder()
        .group("promotions")
        .displayName("Promotions & Discounts")
        .pathsToMatch("/promotions/**", "/coupons/**")
        .build();
  }

  /**
   * Templates module API group.
   * Includes: page templates, blocks, versioning, SEO metadata
   */
  @Bean
  public GroupedOpenApi templatesApi() {
    return GroupedOpenApi.builder()
        .group("templates")
        .displayName("Page Templates")
        .pathsToMatch("/templates/**")
        .build();
  }

  /**
   * Admin module API group.
   * Includes: system administration, monitoring, analytics
   */
  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .displayName("Administration")
        .pathsToMatch("/admin/**")
        .build();
  }

  /**
   * Health & Actuator endpoints group.
   * Includes: health checks, metrics, environment info
   */
  @Bean
  public GroupedOpenApi actuatorApi() {
    return GroupedOpenApi.builder()
        .group("actuator")
        .displayName("Health & Monitoring")
        .pathsToMatch("/actuator/**")
        .build();
  }
}
