package org.sirantar.recadero;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Shared Spring Boot test bootstrap for backend integration tests.
 */
@SpringBootTest(
    classes = RecaderoApplication.class
)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractSpringBootTest {

  @MockBean
  protected JwtDecoder jwtDecoder;
}
