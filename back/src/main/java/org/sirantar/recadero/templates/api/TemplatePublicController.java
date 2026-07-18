package org.sirantar.recadero.templates.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.sirantar.recadero.templates.service.TemplateService;
import org.sirantar.recadero.templates.service.dto.TemplateDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public template rendering (no auth) and admin preview (any status/version).
 * Mounted under /api/templates (no /v1 segment), per templates-contract.md
 * and SecurityConfig's public-endpoint matcher for GET /api/templates/*.
 */
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplatePublicController {

  private final TemplateService templateService;

  @GetMapping("/{slug}")
  public TemplateDetail getPublishedTemplate(@PathVariable String slug) {
    return templateService.getPublishedBySlug(slug);
  }

  @AdminOnly
  @GetMapping("/preview/{id}")
  public TemplateDetail previewTemplate(
      @PathVariable UUID id, @RequestParam(required = false) Integer version) {
    return templateService.previewTemplate(id, version);
  }
}
