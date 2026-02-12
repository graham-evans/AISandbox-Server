/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

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

/**
 * Integration tests for running the Mancala simulation.
 */
public class TestRunMancala {

  /**
   * Tests the Mancala simulation with different themes.
   *
   * @param theme the theme to test with
   */
  @ParameterizedTest
  @EnumSource(Theme.class)
  public void testRunMancalaGame(Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      MancalaBuilder builder = new MancalaBuilder();
      builder.setSeedsPerPit(MancalaSeedsPerPit.FOUR);
      // create players
      List<Agent> agents = Arrays.stream(builder.getAgentNames(2))
          .map(s -> (Agent) new MockMancalaPlayer(s)).toList();
      // create simulation
      Simulation sim = builder.build(agents, theme, new Random());
      // create output directory
      File outputDirectory = new File("build/test/mancala/" + theme.name().toLowerCase());
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
