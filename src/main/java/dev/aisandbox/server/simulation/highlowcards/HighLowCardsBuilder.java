/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import java.util.List;
import java.util.Random;

/**
 * Builder class for the High-Low Cards game simulation. This class implements SimulationBuilder to
 * provide the configuration and instantiation of the HighLowCards simulation.
 *
 * <p>The High-Low Cards game presents a series of playing cards to the player, who must predict
 * whether the next card will be higher or lower than the current one.
 */
public final class HighLowCardsBuilder implements SimulationBuilder {

  /**
   * Returns the name of this simulation.
   *
   * @return the string "HighLowCards" as the simulation name
   */
  @Override
  public String getSimulationName() {
    return "HighLowCards";
  }

  /**
   * Provides a brief description of the High-Low Cards simulation.
   *
   * @return a string describing the simulation and its basic rules
   */
  @Override
  public String getDescription() {
    return "Simulation to guess if the next card is 'higher' or 'lower' than the card before. "
        + "Note: You don't get anything for a pair, not in this game!";
  }

  /**
   * Returns the configurable parameters for this simulation. Currently, the High-Low Cards game
   * doesn't have any configurable parameters.
   *
   * @return an empty list, as there are no configurable parameters
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(); // No configurable parameters for this simulation
  }

  /**
   * Specifies the minimum number of agents required for this simulation. High-Low Cards is a
   * single-player game, so only one agent is needed.
   *
   * @return 1 - the minimum number of agents
   */
  @Override
  public int getMinAgentCount() {
    return 1;
  }

  /**
   * Specifies the maximum number of agents allowed for this simulation. High-Low Cards is a
   * single-player game, so only one agent is supported.
   *
   * @return 1 - the maximum number of agents
   */
  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  /**
   * Provides the names for agents participating in the simulation.
   *
   * @param agentCount the number of agents in the simulation (always 1 for this game)
   * @return an array of agent names, with a single entry "Agent 1"
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  /**
   * Builds and returns a new instance of the High-Low Cards simulation.
   *
   * @param agents a list of agents that will participate (only the first is used)
   * @param theme  the visual theme to be applied to the simulation
   * @param random a random number generator to be used for game randomization
   * @return a new HighLowCards simulation instance
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    // Create a new simulation with the first agent, 9 cards, the specified theme, and the random
    // seed
    return new HighLowCards(agents.getFirst(), 9, theme, random);
  }
}
