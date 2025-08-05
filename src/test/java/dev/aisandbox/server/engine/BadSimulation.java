/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import java.util.List;
import java.util.Random;

/**
 * Test simulation builder that provides invalid parameters for negative testing.
 * This class is used to test parameter validation and error handling.
 */
public class BadSimulation implements SimulationBuilder {

  @Override
  public String getSimulationName() {
    return "Bad Simulation";
  }

  @Override
  public String getDescription() {
    return "Simulation to negatively test parameters";
  }

  @Override
  public List<SimulationParameter> getParameters() {
    return List.of(new SimulationParameter("height", "Non existent parameter", Integer.class));
  }

  @Override
  public int getMinAgentCount() {
    return 0;
  }

  @Override
  public int getMaxAgentCount() {
    return 0;
  }

  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[0];
  }

  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random) {
    return null;
  }
}
