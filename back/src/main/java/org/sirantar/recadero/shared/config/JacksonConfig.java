package org.sirantar.recadero.shared.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson ObjectMapper configuration for consistent JSON serialization/deserialization.
 *
 * Provides:
 * - LocalDateTime serialization in ISO 8601 format (UTC)
 * - Null field handling (excluded from response)
 * - Feature toggles for robustness and compatibility
 */
@Configuration
public class JacksonConfig {

  private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Register Java 8+ time module for LocalDateTime, ZonedDateTime, etc.
    mapper.registerModule(new JavaTimeModule());

    // Register custom serializers/deserializers
    mapper.registerModule(createCustomTimeModule());

    // === Serialization Features ===
    // Exclude null fields from JSON output
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Write dates as ISO-8601 strings (not timestamps)
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Pretty-print JSON in development (can be toggled via profile)
    // mapper.enable(SerializationFeature.INDENT_OUTPUT);

    // === Deserialization Features ===
    // Allow deserialization of unknown properties (graceful degradation)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // Allow empty string to NULL coercion
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    // Allow coercion of scalar values to arrays (e.g., single ID → [ID])
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    // Fail on reading a date value that is malformed
    mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

    return mapper;
  }

  /**
   * Custom module for LocalDateTime serialization/deserialization.
   *
   * Ensures consistent handling of LocalDateTime across all API responses:
   * - Output format: ISO-8601 with UTC zone (e.g., "2026-05-19T21:15:45.123Z")
   * - Input format: ISO-8601 string parsed to LocalDateTime
   */
  private SimpleModule createCustomTimeModule() {
    SimpleModule module = new SimpleModule("customTimeModule");

    // LocalDateTime Serializer: YYYY-MM-DDTHH:MM:SS.sssZ
    module.addSerializer(
        LocalDateTime.class,
        new StdSerializer<LocalDateTime>(LocalDateTime.class) {
          @Override
          public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
              gen.writeNull();
            } else {
              // Convert to ZonedDateTime in UTC for consistent timezone representation
              ZonedDateTime zdt = value.atZone(ZoneId.of("UTC"));
              gen.writeString(zdt.format(ISO_8601_FORMATTER));
            }
          }
        }
    );

    // LocalDateTime Deserializer: parse ISO-8601 string to LocalDateTime
    module.addDeserializer(
        LocalDateTime.class,
        new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
          @Override
          public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
              return null;
            }
            try {
              // Parse ISO-8601 string and extract LocalDateTime
              return LocalDateTime.parse(value, ISO_8601_FORMATTER);
            } catch (Exception e) {
              ctxt.reportInputMismatch(LocalDateTime.class, "Unable to parse LocalDateTime from: " + value);
              return null;
            }
          }
        }
    );

    return module;
  }
}
