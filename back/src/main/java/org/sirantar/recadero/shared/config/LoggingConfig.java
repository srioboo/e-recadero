package org.sirantar.recadero.shared.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Centralized logging configuration for the backend application.
 *
 * Applies profile-aware Logback levels so development stays verbose while
 * production remains quiet and operationally safe.
 */
@Configuration
public class LoggingConfig {

  private static final List<String> DEBUG_PACKAGES = Arrays.asList(
      "org.sirantar.recadero",
      "org.springframework.web",
      "org.springframework.security",
      "org.springframework.data",
      "org.hibernate",
      "org.hibernate.SQL",
      "org.hibernate.type.descriptor.sql.BasicBinder"
  );

  private static final String PROD_PROFILE = "prod";
  private static final String DEV_PROFILE = "dev";

  private final Environment environment;

  public LoggingConfig(Environment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void configureLogLevels() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    boolean productionProfileActive = isProfileActive(PROD_PROFILE);
    boolean developmentProfileActive = isProfileActive(DEV_PROFILE);

    Level rootLevel = productionProfileActive ? Level.WARN : Level.INFO;
    setLoggerLevel(loggerContext, "root", rootLevel);

    if (productionProfileActive) {
      setLoggerLevel(loggerContext, "org.sirantar.recadero", Level.WARN);
      setLoggerLevel(loggerContext, "org.springframework.web", Level.WARN);
      setLoggerLevel(loggerContext, "org.springframework.security", Level.WARN);
      setLoggerLevel(loggerContext, "org.springframework.data", Level.WARN);
      setLoggerLevel(loggerContext, "org.hibernate", Level.WARN);
      setLoggerLevel(loggerContext, "org.hibernate.SQL", Level.WARN);
      setLoggerLevel(loggerContext, "org.hibernate.type.descriptor.sql.BasicBinder", Level.WARN);
      return;
    }

    if (developmentProfileActive) {
      setLoggerLevel(loggerContext, "org.sirantar.recadero", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.springframework.web", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.springframework.security", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.springframework.data", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.hibernate", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.hibernate.SQL", Level.DEBUG);
      setLoggerLevel(loggerContext, "org.hibernate.type.descriptor.sql.BasicBinder", Level.TRACE);
    }
  }

  private boolean isProfileActive(String profile) {
    return StringUtils.hasText(profile) && environment != null && Arrays.asList(environment.getActiveProfiles()).contains(profile);
  }

  private void setLoggerLevel(LoggerContext context, String loggerName, Level level) {
    Logger logger = "root".equals(loggerName)
        ? context.getLogger(Logger.ROOT_LOGGER_NAME)
        : context.getLogger(loggerName);
    logger.setLevel(level);
  }
}
