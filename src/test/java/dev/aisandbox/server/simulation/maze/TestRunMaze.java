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

public class TestRunMaze {

  private final static File outputDirectory = new File("build/test/maze");


  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

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
          .map(s -> (Agent) new MockMazeAgent(s)).toList();
      // create simulation
      Simulation sim = builder.build(agents, Theme.LIGHT, new Random());
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
