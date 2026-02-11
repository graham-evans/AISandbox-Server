/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

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
 * A simulation builder for the Mine Hunter scenario.
 *
 * <p>This class implements the {@link SimulationBuilder} interface to create instances of the Mine
 * Hunter simulation. Mine Hunter is a game where an AI agent must locate mines hidden in a grid
 * using numerical clues, similar to the classic Minesweeper game.
 *
 * <p>The simulation can be configured with different board sizes, affecting difficulty.
 */
public final class MineHunterScenario implements SimulationBuilder {

  /**
   * The size configuration for the mine field.
   *
   * <p>This property determines the dimensions of the board and number of mines. Defaults to MEDIUM
   * size (16x16 with 40 mines).
   */
  @Getter
  @Setter
  MineSize mineSize = MineSize.MEDIUM;

  /**
   * Returns the name of the simulation.
   *
   * @return The string "MineHunter" as the name of this simulation
   */
  @Override
  public String getSimulationName() {
    return "MineHunter";
  }

  /**
   * Provides a detailed description of the Mine Hunter simulation.
   *
   * @return A string describing the objectives and mechanics of the Mine Hunter game
   */
  @Override
  public String getDescription() {
    return "Find the mines in a grid using deduction. Mine Hunter pits the AI against a "
        + "minefield! A known quantity of mines have been distributed across a grid of squares "
        + "and the AI agent must work out where they are. To help, each uncovered square will "
        + "show how many mines are in the surrounding squares.";
  }

  /**
   * Returns the configurable parameters for this simulation.
   *
   * <p>For Mine Hunter, the only configurable parameter is the mine field size.
   *
   * @return A list containing the mine size parameter
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(
        new SimulationParameter("mineSize", "The size of the minefield", MineSize.class));
  }

  /**
   * Returns the minimum number of agents required for this simulation.
   *
   * @return 1, as this simulation requires exactly one agent
   */
  @Override
  public int getMinAgentCount() {
    return 1;
  }

  /**
   * Returns the maximum number of agents supported by this simulation.
   *
   * @return 1, as this simulation supports exactly one agent
   */
  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  /**
   * Provides names for the agents in this simulation.
   *
   * <p>Since this simulation only supports one agent, it returns a single name.
   *
   * @param agentCount The number of agents (will always be 1 for this simulation)
   * @return An array containing the name for the single agent
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  /**
   * Creates a new instance of the Mine Hunter simulation.
   *
   * <p>This method builds a runtime instance of the simulation with the provided agent, theme, and a
   * random number generator for game state generation.
   *
   * @param agents The list of agents participating in the simulation (only the first one is used)
   * @param theme  The visual theme to use for the simulation
   * @param random A random number generator for creating the mine field
   * @return A new {@link MineHunterRuntime} instance
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new MineHunterRuntime(agents.getFirst(), mineSize, theme, random);
  }
}
