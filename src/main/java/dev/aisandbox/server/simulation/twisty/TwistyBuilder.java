/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * TwistyBuilder implements SimulationBuilder to create twisty puzzle simulations.
 * 
 * <p>This class is responsible for building simulations that involve various twisty puzzles,
 * such as Rubik's Cubes of different dimensions, Pyraminx, and other twisting puzzles.
 * The builder allows configuration of puzzle type and starting state (solved or scrambled).</p>
 *
 * @author gde
 * @version $Id: $Id
 */
@Setter
@Getter
@Slf4j
public final class TwistyBuilder implements SimulationBuilder {

  /**
   * The type of puzzle to create in the simulation.
   * Defaults to a standard 3x3 Rubik's Cube.
   */
  private PuzzleType puzzleType = PuzzleType.CUBE3;

  /**
   * Flag indicating whether the puzzle should start in a solved state.
   * When false (default), the puzzle will be randomly scrambled at start.
   */
  private Boolean startSolved = false;

  /**
   * {@inheritDoc}
   * 
   * @return The name of this simulation type ("Twisty")
   */
  @Override
  public String getSimulationName() {
    return "Twisty";
  }

  /**
   * {@inheritDoc}
   * 
   * @return A short description of the simulation
   */
  @Override
  public String getDescription() {
    return "Solve various twisting puzzles.";
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Defines the configurable parameters for this simulation:
   * <ul>
   *   <li>puzzleType - The type of twisty puzzle to solve</li>
   *   <li>startSolved - Whether the puzzle starts in a solved state</li>
   * </ul>
   * </p>
   * 
   * @return List of configurable simulation parameters
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(
        new SimulationParameter("puzzleType", "The design of the puzzle", PuzzleType.class),
        new SimulationParameter("startSolved", "Start with a solved puzzle", Boolean.class));
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Twisty puzzles require exactly one agent to solve them.</p>
   * 
   * @return The minimum number of agents required (1)
   */
  @Override
  public int getMinAgentCount() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Twisty puzzles require exactly one agent to solve them.</p>
   * 
   * @return The maximum number of agents supported (1)
   */
  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Returns the default name for the agent in this simulation.</p>
   * 
   * @param agentCount The number of agents in the simulation
   * @return An array containing the name for the single agent
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Builds a new TwistySimulation with the configured parameters.</p>
   * 
   * @param agents The list of agents participating in the simulation
   * @param theme The visual theme for the simulation
   * @param random A random number generator for state initialization
   * @return A new TwistySimulation instance, or null if an error occurs during creation
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    try {
      return new TwistySimulation(agents.getFirst(), puzzleType.getTwistyPuzzle(), startSolved,
          theme, random);
    } catch (Exception e) {
      log.error("Error while building Twisty Runtime.", e);
      return null;
    }
  }
}
