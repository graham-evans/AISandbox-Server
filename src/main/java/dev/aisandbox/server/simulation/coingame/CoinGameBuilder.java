/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

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
 * Simulation builder for the Coin game simulation, as explained in the book AlphaGo Simplified.
 * <p>
 * The Coin Game is a classic mathematical game where players take turns to remove coins from piles.
 * This implementation supports different scenarios (defined by {@link CoinScenario}) that vary in
 * the number of piles and coins, as well as allowed moves.
 * <p>
 * In all scenarios, the player forced to take the last coin loses the game.
 */
public final class CoinGameBuilder implements SimulationBuilder {

  /**
   * The selected scenario for this coin game. Default is SINGLE_21_2, which represents a single
   * pile of 21 coins where players can take 1 or 2 coins per turn.
   */
  @Getter
  @Setter
  private CoinScenario scenario = CoinScenario.SINGLE_21_2;

  /**
   * Returns the name of the simulation.
   *
   * @return The string "CoinGame" as the simulation name
   */
  @Override
  public String getSimulationName() {
    return "CoinGame";
  }

  /**
   * Provides a description of the coin game simulation.
   *
   * @return A string describing the coin game rules and objective
   */
  @Override
  public String getDescription() {
    return "Various versions of the 'Coin Game' where agents take turns to remove coins from one "
        + "or more piles. The loser is the one who it forced to take the last coin.";
  }

  /**
   * Provides the configurable parameters for this simulation.
   * <p>
   * Currently only supports selecting the scenario to run.
   *
   * @return A list containing the available simulation parameters
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(new SimulationParameter("scenario", "The scenario to run", CoinScenario.class));
  }

  /**
   * Returns the minimum number of agents required for this simulation.
   *
   * @return 2, as the coin game requires exactly two players
   */
  @Override
  public int getMinAgentCount() {
    return 2;
  }

  /**
   * Returns the maximum number of agents supported by this simulation.
   *
   * @return 2, as the coin game is designed for exactly two players
   */
  @Override
  public int getMaxAgentCount() {
    return 2;
  }

  /**
   * Returns names for the agents in this simulation.
   *
   * @param agentCount The number of agents (should always be 2 for this simulation)
   * @return An array of generic agent names
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1", "Agent 2"};
  }

  /**
   * Builds and returns a new CoinGame simulation instance.
   * <p>
   * Creates the simulation with the configured scenario and provided agents.
   *
   * @param agents The list of agents that will participate in the simulation
   * @param theme  The visual theme to apply to the simulation
   * @param random A random number generator for any stochastic elements
   * @return A new CoinGame simulation instance
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new CoinGame(agents, scenario, theme);
  }
}
