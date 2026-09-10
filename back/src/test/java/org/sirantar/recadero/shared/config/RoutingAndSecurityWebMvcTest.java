package org.sirantar.recadero.shared.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sirantar.recadero.catalog.api.CategoryController;
import org.sirantar.recadero.catalog.service.CatalogService;
import org.sirantar.recadero.catalog.service.dto.CategoryResponse;
import org.sirantar.recadero.shared.config.properties.SecurityProperties;
import org.sirantar.recadero.shared.security.SecurityExpressionRoot;
import org.sirantar.recadero.users.api.AuthController;
import org.sirantar.recadero.users.service.UserAuthenticationService;
import org.sirantar.recadero.users.service.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Empirical proof for the T191 code-review finding: {@code server.servlet.context-path}
 * is {@code /api/v1}, and Spring both strips it before matching {@code SecurityConfig}'s
 * {@code requestMatchers} and does NOT include it in {@code @RequestMapping} values. Every
 * controller and {@code SecurityConfig} pattern previously baked in a redundant
 * {@code /api/v1} prefix, so — despite looking correct in isolation — no request would have
 * actually reached its intended matcher once deployed behind the real context-path. This
 * test exercises real {@code DispatcherServlet} + {@code SecurityFilterChain} routing
 * (unlike every other test in this project, which calls services directly and never goes
 * through HTTP) to confirm the fix, and that it hasn't regressed.
 *
 * <p>{@code GET /categories} used to be the "protected endpoint" exercised here, but it is
 * now intentionally public (catalog browsing does not require a login) — see
 * {@link SecurityConfig}. {@code POST /categories/{id}/move} takes over that role: it still
 * requires both authentication and the {@code ADMIN} role.
 */
@WebMvcTest(controllers = {AuthController.class, CategoryController.class})
@Import({SecurityConfig.class, SecurityProperties.class, SecurityExpressionRoot.class})
@TestPropertySource(properties = {
    "server.servlet.context-path=/api/v1",
    "app.security.jwt.secret=test-secret-key-at-least-32-bytes-long-for-hmac"
})
class RoutingAndSecurityWebMvcTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserAuthenticationService authenticationService;
  @MockBean private CatalogService catalogService;

  @Test
  void publicLoginEndpointIsReachableWithoutAuthentication() throws Exception {
    when(authenticationService.login("a@b.com", "pw"))
        .thenReturn(new LoginResponse("access", "refresh", 900, null));

    mockMvc.perform(post("/api/v1/auth/login")
            .contextPath("/api/v1")
            .contentType("application/json")
            .content("{\"email\":\"a@b.com\",\"password\":\"pw\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void publicCatalogEndpointIsReachableWithoutAuthentication() throws Exception {
    when(catalogService.listCategories(any())).thenReturn(org.springframework.data.domain.Page.empty());

    mockMvc.perform(get("/api/v1/categories").contextPath("/api/v1"))
        .andExpect(status().isOk());
  }

  @Test
  void protectedEndpointRejectsAnonymousRequests() throws Exception {
    mockMvc.perform(post("/api/v1/categories/1/move")
            .contextPath("/api/v1")
            .contentType("application/json")
            .content("{\"parentCategoryId\":2}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpointRejectsAuthenticatedNonAdminRequests() throws Exception {
    mockMvc.perform(post("/api/v1/categories/1/move")
            .contextPath("/api/v1")
            .contentType("application/json")
            .content("{\"parentCategoryId\":2}")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void protectedEndpointAcceptsAuthenticatedAdminRequests() throws Exception {
    when(catalogService.moveCategory(eq(1L), any()))
        .thenReturn(new CategoryResponse(1L, "Electronics", "electronics", null, 2L, null, true, 0, List.of()));

    mockMvc.perform(post("/api/v1/categories/1/move")
            .contextPath("/api/v1")
            .contentType("application/json")
            .content("{\"parentCategoryId\":2}")
            // mockJwt() bypasses JwtAuthenticationConverter, which prefixes JWT "roles"
            // claims with "ROLE_" — so the authority must already carry that prefix here
            // to match what a real request authenticated by that converter would produce.
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }
}
