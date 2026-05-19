package org.sirantar.recadero.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Spring MVC configuration for HTTP message conversion and request/response processing.
 *
 * Provides:
 * - Custom message converters with Jackson ObjectMapper
 * - Request/response logging interceptor
 * - Request timing interceptor for performance monitoring
 * - Content negotiation for JSON/XML endpoints
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

  private final ObjectMapper objectMapper;

  public WebMvcConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Configure HTTP message converters.
   *
   * Ensures Jackson ObjectMapper is used for all JSON serialization/deserialization.
   * Order matters: more specific converters first (JSON), then generic ones (form-encoded).
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // JSON converter with custom ObjectMapper (includes LocalDateTime config, null handling)
    MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
    jacksonConverter.setSupportedMediaTypes(Arrays.asList(
        MediaType.APPLICATION_JSON,
        new MediaType("application", "*+json")
    ));
    converters.add(jacksonConverter);
  }

  /**
   * Register interceptors for cross-cutting concerns.
   *
   * Interceptors are applied in order: timing first (outer), then logging (inner).
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // Timing interceptor: measure request processing duration
    registry.addInterceptor(new TimingInterceptor())
        .addPathPatterns("/api/**", "/actuator/**");

    // Logging interceptor: log request details and response status
    registry.addInterceptor(new LoggingInterceptor())
        .addPathPatterns("/api/**", "/actuator/**")
        .excludePathPatterns("/actuator/health"); // Health check is too noisy
  }

  /**
   * Interceptor for request/response logging.
   *
   * Logs:
   * - HTTP method, URI, remote IP
   * - Response status code
   * - Suppressed for health checks to avoid log spam
   */
  public static class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger requestLog = LoggerFactory.getLogger(
        "org.sirantar.recadero.http.access"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      request.setAttribute("http.request.startTime", System.currentTimeMillis());
      return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
      Long startTime = (Long) request.getAttribute("http.request.startTime");
      long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

      String logMessage = String.format(
          "%s %s from %s - Status: %d",
          request.getMethod(),
          request.getRequestURI(),
          request.getRemoteAddr(),
          response.getStatus()
      );

      if (duration > 0) {
        logMessage += String.format(" (%.0fms)", (double) duration);
      }

      // Log level based on response status
      if (response.getStatus() >= 500) {
        requestLog.error(logMessage);
      } else if (response.getStatus() >= 400) {
        requestLog.warn(logMessage);
      } else {
        requestLog.info(logMessage);
      }

      if (ex != null) {
        requestLog.error("Request failed with exception", ex);
      }
    }
  }

  /**
   * Interceptor for request timing/performance monitoring.
   *
   * Tracks:
   * - Request processing duration (wall-clock time)
   * - Exposes duration via response header "X-Response-Time"
   * - Logs slow requests (> 1000ms threshold)
   */
  public static class TimingInterceptor implements HandlerInterceptor {

    private static final Logger performanceLog = LoggerFactory.getLogger(
        "org.sirantar.recadero.http.performance"
    );
    private static final long SLOW_REQUEST_THRESHOLD = 1000; // milliseconds

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      request.setAttribute("timing.startNano", System.nanoTime());
      return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
      Long startNano = (Long) request.getAttribute("timing.startNano");
      if (startNano == null) {
        return;
      }

      long durationNano = System.nanoTime() - startNano;
      long durationMs = durationNano / 1_000_000;

      // Add response header for client-side performance tracking
      response.addHeader("X-Response-Time", durationMs + "ms");

      // Log slow requests for server-side monitoring
      if (durationMs > SLOW_REQUEST_THRESHOLD) {
        performanceLog.warn(
            "Slow request: {} {} took {}ms (threshold: {}ms)",
            request.getMethod(),
            request.getRequestURI(),
            durationMs,
            SLOW_REQUEST_THRESHOLD
        );
      }
    }
  }
}
