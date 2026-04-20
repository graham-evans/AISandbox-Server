/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

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
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests for the Cascade simulation.
 *
 * <p>Each test runs the simulation for three complete episodes (90 moves) using a
 * {@link MockCascadeAgent} that only submits valid swaps. A rendered frame is written every
 * 10 steps so the visual output can be inspected after the test run.
 */
public class TestRunCascade {

  private static final File OUTPUT_DIR = new File("build/test/cascade");

  /** Creates the output directory before any tests run. */
  @BeforeAll
  static void setupDir() {
    OUTPUT_DIR.mkdirs();
  }

  /**
   * Provides one set of arguments per theme.
   *
   * @return a stream of {@link Theme} arguments
   */
  static Stream<Arguments> allThemesProvider() {
    return Arrays.stream(Theme.values()).map(Arguments::of);
  }

  /**
   * Runs three full episodes of Cascade using a valid-move-only agent and verifies that no
   * exception is thrown. Rendered frames are written to {@code build/test/cascade/<theme>}.
   *
   * @param theme the visual theme to render with
   */
  @ParameterizedTest
  @MethodSource("allThemesProvider")
  void testRunCascade(Theme theme) {
    assertDoesNotThrow(() -> {
      CascadeScenario builder = new CascadeScenario();
      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(name -> (Agent) new MockCascadeAgent(name))
          .toList();

      Simulation sim = builder.build(agents, theme, new Random());

      File targetDir = new File(OUTPUT_DIR, theme.name().toLowerCase());
      targetDir.mkdirs();

      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(10);
      out.setOutputDirectory(targetDir);
      out.setup(sim);

      // Run three complete episodes (3 × 30 moves)
      for (int step = 0; step < 90; step++) {
        sim.step(out);
      }

      sim.close();
      agents.forEach(Agent::close);
    });
  }
}
