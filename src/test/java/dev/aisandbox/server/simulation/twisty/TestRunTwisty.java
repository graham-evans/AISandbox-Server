/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

/** Tests for running the Twisty puzzle simulation. */
public class TestRunTwisty {

  private static final File outputDirectory = new File("build/test/twisty");

  /** Initializes the output directory for Twisty tests. */
  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

  /**
   * Provides all combinations of puzzle types and themes for testing.
   *
   * @return stream of test arguments
   */
  static Stream<Arguments> allTwistyProvider() {
    Stream.Builder<Arguments> arguments = Stream.builder();
    for (PuzzleType puzzle : PuzzleType.values()) {
      for (Theme theme : Theme.values()) {
        arguments.add(Arguments.of(puzzle, theme));
      }
    }
    return arguments.build();
  }

  /**
   * Tests running the Twisty puzzle with different puzzle types and themes.
   *
   * @param puzzleType the type of puzzle to test
   * @param theme the theme to render with
   */
  @ParameterizedTest
  @MethodSource("allTwistyProvider")
  public void testRunTwisty(PuzzleType puzzleType, Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      TwistyBuilder builder = new TwistyBuilder();
      builder.setPuzzleType(puzzleType);
      builder.setStartSolved(false);
      // create players
      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new MockTwistyAgent(s)).toList();
      // create simulation
      Simulation sim = builder.build(agents, theme, new Random());
      assertNotNull(sim);
      // create output
      File targetDir = new File(outputDirectory,
          puzzleType.name() + "_" + theme.name().toLowerCase());
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
