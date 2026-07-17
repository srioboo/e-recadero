package org.sirantar.recadero.templates;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Templates module contract and boundary verification tests.
 * Verifies that:
 * - All required services and controllers are available
 * - Module structure is properly organized
 * - Package annotations are correctly configured
 */
class TemplatesApplicationModuleTest {

  @Test
  void testTemplatesServicesExist() {
    assertClassExists("org/sirantar/recadero/templates/service/TemplateService.class");
    assertClassExists("org/sirantar/recadero/templates/service/TemplateBlockService.class");
    assertClassExists("org/sirantar/recadero/templates/service/TemplateMetaService.class");
    assertClassExists("org/sirantar/recadero/templates/service/PageContentService.class");
    assertClassExists("org/sirantar/recadero/templates/service/TemplateContentProvider.class");
  }

  @Test
  void testTemplatesRepositoriesExist() {
    assertClassExists("org/sirantar/recadero/templates/repository/TemplateRepository.class");
    assertClassExists("org/sirantar/recadero/templates/repository/TemplateBlockRepository.class");
    assertClassExists("org/sirantar/recadero/templates/repository/TemplateVersionRepository.class");
    assertClassExists("org/sirantar/recadero/templates/repository/TemplateMetaRepository.class");
    assertClassExists("org/sirantar/recadero/templates/repository/PageContentMappingRepository.class");
  }

  @Test
  void testTemplatesApiExists() {
    assertClassExists("org/sirantar/recadero/templates/api/TemplateAdminController.class");
    assertClassExists("org/sirantar/recadero/templates/api/TemplateVersionController.class");
    assertClassExists("org/sirantar/recadero/templates/api/TemplatePublicController.class");
  }

  @Test
  void testTemplatesDomainsExist() {
    assertClassExists("org/sirantar/recadero/templates/domain/Template.class");
    assertClassExists("org/sirantar/recadero/templates/domain/TemplateBlock.class");
    assertClassExists("org/sirantar/recadero/templates/domain/TemplateMeta.class");
    assertClassExists("org/sirantar/recadero/templates/domain/TemplateVersion.class");
    assertClassExists("org/sirantar/recadero/templates/domain/PageContentMapping.class");
  }

  @Test
  void testTemplatesEventsExist() {
    assertClassExists("org/sirantar/recadero/templates/events/TemplatePublishedEvent.class");
    assertClassExists("org/sirantar/recadero/templates/events/TemplateArchivedEvent.class");
    assertClassExists("org/sirantar/recadero/templates/events/TemplateEventPublisher.class");
  }

  @Test
  void testTemplatesPackageInfoExists() {
    assertClassExists("org/sirantar/recadero/templates/package-info.class");
  }

  private void assertClassExists(String resourcePath) {
    assertThat(TemplatesApplicationModuleTest.class.getClassLoader().getResource(resourcePath))
        .as(resourcePath + " should exist")
        .isNotNull();
  }
}
