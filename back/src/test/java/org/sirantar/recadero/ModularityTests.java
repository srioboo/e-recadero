package org.sirantar.recadero;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Structural module-boundary verification (T191): unlike the per-module
 * {@code *ApplicationModuleTest} classes (which only assert that expected
 * classes exist), this actually asks Spring Modulith to parse the compiled
 * package structure, cross-check it against each module's
 * {@code allowedDependencies}, and fail on cycles or undeclared coupling.
 */
class ModularityTests {

  @Test
  void modulesAreConsistent() {
    ApplicationModules modules = ApplicationModules.of(RecaderoApplication.class);
    modules.verify();
  }
}
