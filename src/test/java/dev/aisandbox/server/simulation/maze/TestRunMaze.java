/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

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

/** Tests for running the Maze simulation. */
public class TestRunMaze {

  private static final File outputDirectory = new File("build/test/maze");

  /** Initializes the output directory for Maze tests. */
  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

  /**
   * Provides all combinations of maze types, sizes, and themes for testing.
   *
   * @return stream of test arguments
   */
  static Stream<Arguments> allMazeProvider() {
    Stream.Builder<Arguments> arguments = Stream.builder();
    for (MazeType mazeType : MazeType.values()) {
      for (MazeSize mazeSize : MazeSize.values()) {
        for (Theme theme : Theme.values()) {
          arguments.add(Arguments.of(mazeType, mazeSize, theme));
        }
      }
    }
    return arguments.build();
  }

  /**
   * Tests running the Maze simulation with different types, sizes, and themes.
   *
   * @param mazeType the maze type to test
   * @param mazeSize the maze size to test
   * @param theme the theme to render with
   */
  @ParameterizedTest
  @MethodSource("allMazeProvider")
  public void testRunMaze(MazeType mazeType, MazeSize mazeSize, Theme theme) {
    assertDoesNotThrow(() -> {
      // create simulation
      MazeBuilder builder = new MazeBuilder();
      builder.setMazeSize(mazeSize);
      builder.setMazeType(mazeType);
      // create players
      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new QLearningMazeAgent(s)).toList();
      // create simulation
      Simulation sim = builder.build(agents, theme, new Random());
      // create output
      File targetDir = new File(outputDirectory,
          mazeType.name() + "-" + mazeSize.name() + "-" + theme.name().toLowerCase());
      targetDir.mkdirs();
      OutputRenderer out = new BitmapOutputRenderer();
      out.setSkipFrames(1000);
      out.setOutputDirectory(targetDir);
      out.setup(sim);
      // start simulation
      for (int step = 0; step < 10000; step++) {
        sim.step(out);
      }
      // finish simulation
      sim.close();
      agents.forEach(Agent::close);
    });
  }
}
