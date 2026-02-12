/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation;

import dev.aisandbox.launcher.SandboxServerCLIApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for documentation generation for simulations. */
@Slf4j
public class DocumentationTests {

  /**
   * Tests documentation generation for each simulation type.
   *
   * @param simulationEnumeration the simulation type to test
   */
  @ParameterizedTest
  @EnumSource(SimulationEnumeration.class)
  public void testDocumentationGeneration(SimulationEnumeration simulationEnumeration) {
    log.info("Testing generation of docs for {}", simulationEnumeration.name());

    SandboxServerCLIApplication app = new SandboxServerCLIApplication();

    app.run("--help", "-s", simulationEnumeration.getBuilder().getSimulationName());
  }

  /** Tests the help output listing all available simulations. */
  @Test
  public void testList() {
    SandboxServerCLIApplication app = new SandboxServerCLIApplication();
    app.run("--help");
  }


}
