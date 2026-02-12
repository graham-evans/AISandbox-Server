/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for running the Coin Game simulation. */
public class TestRunCoinGame {

  /**
   * Tests the Coin Game simulation with different themes.
   *
   * @param theme the theme to test with
   */
  @ParameterizedTest
  @EnumSource(Theme.class)
  public void testRunCoinGame(Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      CoinGameBuilder simulationBuilder = new CoinGameBuilder();
      simulationBuilder.setScenario(CoinScenario.NIM);
      // create players
      List<Agent> agents = Arrays.stream(simulationBuilder.getAgentNames(2))
          .map(s -> (Agent) new MockPlayer(s)).toList();
      // create simulation
      Simulation sim = simulationBuilder.build(agents, theme, new Random());
      // create output directory
      File outputDirectory = new File("build/test/coingame/" + theme.name().toLowerCase());
      outputDirectory.mkdirs();
      // create output
      OutputRenderer out = new BitmapOutputRenderer();
      out.setOutputDirectory(outputDirectory);
      out.setup(sim);
      out.setSkipFrames(100);
      // start simulation
      for (int step = 0; step < 1200; step++) {
        sim.step(out);
      }
      // finish simulation
      sim.close();
      agents.forEach(Agent::close);
    });
  }
}
