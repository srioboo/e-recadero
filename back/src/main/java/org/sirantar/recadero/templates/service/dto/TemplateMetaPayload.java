package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * SEO/social metadata, used both as the PUT .../meta request body and
 * nested under "meta" in template responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TemplateMetaPayload(
    @JsonProperty("page_title") String pageTitle,
    @JsonProperty("page_description") String pageDescription,
    @JsonProperty("og_title") String ogTitle,
    @JsonProperty("og_description") String ogDescription,
    @JsonProperty("og_image_url") String ogImageUrl,
    String keywords,
    @JsonProperty("canonical_url") String canonicalUrl,
    @JsonProperty("robots_directive") String robotsDirective,
    @JsonProperty("structured_data_json") Map<String, Object> structuredDataJson) {}
