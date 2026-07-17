package org.sirantar.recadero.templates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.templates.domain.TemplateMeta;
import org.sirantar.recadero.templates.repository.TemplateMetaRepository;
import org.sirantar.recadero.templates.service.dto.TemplateMetaPayload;
import org.sirantar.recadero.templates.service.dto.UpdateMetaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SEO/social metadata management for a template.
 */
@Service
@RequiredArgsConstructor
public class TemplateMetaService {

  private final TemplateMetaRepository templateMetaRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public TemplateMeta createEmpty(UUID templateId) {
    TemplateMeta meta = new TemplateMeta();
    meta.setId(UUID.randomUUID());
    meta.setTemplateId(templateId);
    meta.setUpdatedAt(LocalDateTime.now());
    return templateMetaRepository.save(meta);
  }

  public TemplateMetaPayload getPayload(UUID templateId) {
    return toPayload(getOrCreate(templateId));
  }

  @Transactional
  public UpdateMetaResponse updateMeta(UUID templateId, TemplateMetaPayload payload) {
    TemplateMeta meta = getOrCreate(templateId);
    applyPayload(meta, payload);
    meta.setUpdatedAt(LocalDateTime.now());
    TemplateMeta saved = templateMetaRepository.save(meta);
    return new UpdateMetaResponse(templateId.toString(), toPayload(saved), saved.getUpdatedAt());
  }

  /** Applies only the non-null fields present in a partial update payload. */
  @Transactional
  public void mergePartial(UUID templateId, Map<String, Object> partialMeta) {
    if (partialMeta == null || partialMeta.isEmpty()) {
      return;
    }
    TemplateMeta meta = getOrCreate(templateId);
    if (partialMeta.containsKey("page_title")) meta.setPageTitle((String) partialMeta.get("page_title"));
    if (partialMeta.containsKey("page_description")) {
      meta.setPageDescription((String) partialMeta.get("page_description"));
    }
    if (partialMeta.containsKey("og_title")) meta.setOgTitle((String) partialMeta.get("og_title"));
    if (partialMeta.containsKey("og_description")) meta.setOgDescription((String) partialMeta.get("og_description"));
    if (partialMeta.containsKey("og_image_url")) meta.setOgImageUrl((String) partialMeta.get("og_image_url"));
    if (partialMeta.containsKey("keywords")) meta.setKeywords((String) partialMeta.get("keywords"));
    if (partialMeta.containsKey("canonical_url")) meta.setCanonicalUrl((String) partialMeta.get("canonical_url"));
    if (partialMeta.containsKey("robots_directive")) {
      meta.setRobotsDirective((String) partialMeta.get("robots_directive"));
    }
    meta.setUpdatedAt(LocalDateTime.now());
    templateMetaRepository.save(meta);
  }

  private TemplateMeta getOrCreate(UUID templateId) {
    return templateMetaRepository.findByTemplateId(templateId).orElseGet(() -> createEmpty(templateId));
  }

  private void applyPayload(TemplateMeta meta, TemplateMetaPayload payload) {
    meta.setPageTitle(payload.pageTitle());
    meta.setPageDescription(payload.pageDescription());
    meta.setOgTitle(payload.ogTitle());
    meta.setOgDescription(payload.ogDescription());
    meta.setOgImageUrl(payload.ogImageUrl());
    meta.setKeywords(payload.keywords());
    meta.setCanonicalUrl(payload.canonicalUrl());
    meta.setRobotsDirective(payload.robotsDirective());
    meta.setStructuredDataJson(writeJson(payload.structuredDataJson()));
  }

  private TemplateMetaPayload toPayload(TemplateMeta meta) {
    return new TemplateMetaPayload(
        meta.getPageTitle(),
        meta.getPageDescription(),
        meta.getOgTitle(),
        meta.getOgDescription(),
        meta.getOgImageUrl(),
        meta.getKeywords(),
        meta.getCanonicalUrl(),
        meta.getRobotsDirective(),
        readJson(meta.getStructuredDataJson()));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readJson(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private String writeJson(Map<String, Object> value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid structured_data_json", e);
    }
  }
}
