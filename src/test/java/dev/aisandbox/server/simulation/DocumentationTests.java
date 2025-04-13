package dev.aisandbox.server.simulation;

import dev.aisandbox.launcher.SandboxServerCLIApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Slf4j
public class DocumentationTests {

  @ParameterizedTest
  @EnumSource(SimulationEnumeration.class)
  public void testDocumentationGeneration(SimulationEnumeration simulationEnumeration) {
    log.info("Testing generation of docs for {}", simulationEnumeration.name());

    SandboxServerCLIApplication app = new SandboxServerCLIApplication();

    app.run("--help", "-s", simulationEnumeration.getBuilder().getSimulationName());
  }

  @Test
  public void testList() {
    SandboxServerCLIApplication app = new SandboxServerCLIApplication();
    app.run("--list");
  }


}
