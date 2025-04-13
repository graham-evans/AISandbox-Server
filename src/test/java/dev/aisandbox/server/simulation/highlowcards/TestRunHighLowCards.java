package dev.aisandbox.server.simulation.highlowcards;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.io.File;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class TestRunHighLowCards {

  @ParameterizedTest
  @EnumSource(Theme.class)
  public void testRunHighLowCards(Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      SimulationBuilder simulationBuilder = new HighLowCardsBuilder();
      // create players
      List<Agent> agents = List.of(new MockPlayer());
      // create simulation
      Simulation sim = simulationBuilder.build(agents, theme, new Random());
      // create output directory
      File outputDirectory = new File("build/test/highLowCards/" + theme.name().toLowerCase());
      outputDirectory.mkdirs();
      // create output
      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(100);
      out.setOutputDirectory(outputDirectory);
      out.setup(sim);
      // start simulation
      for (int step = 0; step < 1000; step++) {
        sim.step(out);
      }
      // finish simulation
      sim.close();
      agents.forEach(Agent::close);
    });
  }
}
