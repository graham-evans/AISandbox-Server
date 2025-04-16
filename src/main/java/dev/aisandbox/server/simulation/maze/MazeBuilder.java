package dev.aisandbox.server.simulation.maze;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;

/**
 * A builder for creating a simulation of a maze.
 */
public class MazeBuilder implements SimulationBuilder {

  /**
   * The size of the maze, defaults to MEDIUM.
   */
  @Getter
  @Setter
  private MazeSize mazeSize = MazeSize.MEDIUM;
  /**
   * The type of maze, defaults to BINARYTREE.
   */
  @Getter
  @Setter
  private MazeType mazeType = MazeType.BINARYTREE;

  /**
   * Returns the name of the simulation, which is "Maze".
   */
  @Override
  public String getSimulationName() {
    return "Maze";
  }

  /**
   * Returns a description of the simulation.
   */
  @Override
  public String getDescription() {
    return
        "Navigate the maze and find the exit, then optimise the path to find the shortest route. "
            + "The AI agent will be placed in a Maze and tasked with finding its way to the exit."
            + " Once there it will be rewarded and sent to a random position. "
            + "At each turn the AI agent is given information about the maze (dimensions,"
            + " directions etc),"
            + " the result of the last move (any reward) and asked for the next move. This repeats"
            + " until the episode finished.";
  }

  /**
   * {@inheritDoc}
   * <p>
   * Returns a map of parameters, including mazeSize and mazeType.
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(new SimulationParameter("mazeSize", "The size of the maze", MazeSize.class),
        new SimulationParameter("mazeType", "The style of the maze", MazeType.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * Returns 1 as the minimum agent count for this simulation.
   */
  @Override
  public int getMinAgentCount() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Returns 1 as the maximum agent count for this simulation.
   */
  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Returns an array containing a single string, "Agent 1".
   *
   * @param agentCount The number of players in the simulation (always 1 for this builder).
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  /**
   * {@inheritDoc}
   * <p>
   * Creates a MazeRunner simulation with the specified agents, maze size, maze type, and theme.
   *
   * @param agents The list of agents to use in the simulation (always contains at least one
   *               agent).
   * @param theme  The theme for the simulation.
   * @param random
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new MazeRunner(agents.getFirst(), mazeSize, mazeType, theme, random);
  }
}
