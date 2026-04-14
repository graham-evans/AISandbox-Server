/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

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
 * Builder for the Mancala (Kalah variant) simulation.
 *
 * <p>Mancala is a two-player strategy board game played on a board with two rows of six pits
 * and two stores. Players take turns sowing seeds counter-clockwise, with rules for extra
 * turns and captures. The player with the most seeds in their store wins.
 */
public final class MancalaBuilder implements SimulationBuilder {

  /**
   * The number of seeds placed in each pit at the start of a game. Default is 4, which is the
   * standard Kalah configuration (48 seeds total).
   */
  @Getter
  @Setter
  private MancalaSeedsPerPit seedsPerPit = MancalaSeedsPerPit.FOUR;

  @Override
  public String getSimulationName() {
    return "Mancala";
  }

  @Override
  public String getDescription() {
    return "Mancala (Kalah variant) - a two-player strategy board game. Players take turns "
        + "sowing seeds counter-clockwise around a board of 2x6 pits plus two stores. "
        + "Landing in your store grants an extra turn; landing in an empty pit on your side "
        + "captures the opponent's opposite seeds. The player with the most seeds wins.";
  }

  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(
        new SimulationParameter("seedsPerPit", "Number of seeds per pit at game start",
            MancalaSeedsPerPit.class)
    );
  }

  @Override
  public int getMinAgentCount() {
    return 2;
  }

  @Override
  public int getMaxAgentCount() {
    return 2;
  }

  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Player 1", "Player 2"};
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new MancalaGame(agents, seedsPerPit.getSeeds(), theme);
  }
}
