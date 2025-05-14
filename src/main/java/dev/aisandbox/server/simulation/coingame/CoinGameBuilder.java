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
 */
public final class CoinGameBuilder implements SimulationBuilder {

  @Getter
  @Setter
  private CoinScenario scenario = CoinScenario.SINGLE_21_2;

  @Override
  public String getSimulationName() {
    return "CoinGame";
  }

  @Override
  public String getDescription() {
    return "Various versions of the 'Coin Game' where agents take turns to remove coins from one "
        + "or more piles. The loser is the one who it forced to take the last coin.";
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
  public List<SimulationParameter> getParameters() {
    return List.of(new SimulationParameter("scenario", "The scenario to run", CoinScenario.class));
  }

  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1", "Agent 2"};
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new CoinGame(agents, scenario, theme);
  }


}
