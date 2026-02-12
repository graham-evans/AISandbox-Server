/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

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

/** Tests for running the Mine Hunter simulation. */
public class TestRunMine {

  private static final File outputDirectory = new File("build/test/mine");

  /** Initializes the output directory for Mine tests. */
  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

  /**
   * Provides all combinations of mine sizes and themes for testing.
   *
   * @return stream of test arguments
   */
  static Stream<Arguments> allMineProvider() {
    Stream.Builder<Arguments> arguments = Stream.builder();
    for (MineSize mineSize : MineSize.values()) {
      for (Theme theme : Theme.values()) {
        arguments.add(Arguments.of(mineSize, theme));
      }
    }
    return arguments.build();
  }

  /**
   * Tests running the Mine Hunter simulation with different sizes and themes.
   *
   * @param mineSize the mine field size to test
   * @param theme the theme to render with
   */
  @ParameterizedTest
  @MethodSource("allMineProvider")
  public void testRunMineSize(MineSize mineSize, Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      MineHunterScenario builder = new MineHunterScenario();
      builder.setMineSize(mineSize);
      // create players
      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new MockMineAgent(s)).toList();
      // create simulation
      Simulation sim = builder.build(agents, theme, new Random());
      // create output
      File targetDir = new File(outputDirectory,
          mineSize.name() + "-" + theme.name().toLowerCase());
      targetDir.mkdirs();
      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(100);
      out.setOutputDirectory(targetDir);
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
