/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.simulation.bandit.model.BanditCountEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditNormalEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditPullEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditStdEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public final class BanditScenario implements SimulationBuilder {

  /**
   * How many pulls in each test
   */
  private BanditPullEnumeration banditPulls = BanditPullEnumeration.ONE_HUNDRED;

  /**
   * How the normals for each bandit are chosen
   */
  private BanditNormalEnumeration banditNormal = BanditNormalEnumeration.NORMAL_0_1;

  /**
   * How the std for each bandit are chosen
   */
  private BanditStdEnumeration banditStd = BanditStdEnumeration.ONE;

  /**
   * How the bandits are updated after each step
   */
  private BanditUpdateEnumeration banditUpdate = BanditUpdateEnumeration.FIXED;

  /**
   * The number of bandits to include
   */
  private BanditCountEnumeration banditCount = BanditCountEnumeration.FIVE;

  @Override
  public String getSimulationName() {
    return "Bandit";
  }

  @Override
  public String getDescription() {
    return "The classic 'Multi-Armed Bandit scenario where an agent needs to learn which 'bandit'"
        + " returns the best results.";
  }

  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(new SimulationParameter("banditCount", "The number of bandits",
            BanditCountEnumeration.class),
        new SimulationParameter("banditUpdate", "How bandits change between pulls",
            BanditUpdateEnumeration.class),
        new SimulationParameter("banditStd", "How the standard deviation for each bandit is chosen",
            BanditStdEnumeration.class), new SimulationParameter("banditNormal",
            "How the normal (average) for each bandit is chosen", BanditNormalEnumeration.class),
        new SimulationParameter("banditPulls", "The number of bandit 'pulls' in each episode",
            BanditPullEnumeration.class));
  }

  @Override
  public int getMinAgentCount() {
    return 1;
  }

  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return new BanditRuntime(agents.getFirst(), random, banditCount.getNumber(),
        banditPulls.getNumber(), banditNormal, banditStd, banditUpdate, theme);
  }

}
