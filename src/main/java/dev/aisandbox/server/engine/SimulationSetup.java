/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.network.NetworkAgent;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;

/**
 * Utility class for setting up simulation environments. Provides methods to configure simulations
 * with different agent types.
 */
@UtilityClass
public class SimulationSetup {

  /**
   * Sets up a simulation with network agents, then delegates to the second setupSimulation method.
   * This method creates network agents with sequential ports starting from the defaultPort.
   *
   * @param builder      The simulation builder to use
   * @param agentCount   The number of agents to create
   * @param defaultPort  The starting port number for network agents
   * @param openExternal
   * @param renderer     The output renderer to use
   * @return A configured SimulationRunner ready to start
   * @throws SimulationSetupException If there's an error during simulation setup
   */
  public static SimulationRunner setupSimulation(SimulationBuilder builder, int agentCount,
      int defaultPort, boolean openExternal, OutputRenderer renderer) throws SimulationSetupException {
    AtomicInteger port = new AtomicInteger(defaultPort);
    String[] agentNames = builder.getAgentNames(agentCount);
    List<Agent> agents = new ArrayList<>();
    for (String agentName : agentNames) {
      agents.add(new NetworkAgent(agentName, port.getAndIncrement(), openExternal));
    }
    return setupSimulation(builder, agents, renderer);
  }

  /**
   * Sets up a simulation with pre-configured agents. This method is called by the first
   * setupSimulation method after network agents are created, and can also be called directly when
   * testing with mock agents.
   *
   * @param builder  The simulation builder to use
   * @param agents   The list of pre-configured agents to include in the simulation
   * @param renderer The output renderer to use
   * @return A configured SimulationRunner ready to start
   */
  public static SimulationRunner setupSimulation(SimulationBuilder builder, List<Agent> agents,
      OutputRenderer renderer) {
    // create simulation
    Simulation sim = builder.build(agents, Theme.LIGHT, new Random());
    // start output
    renderer.setup(sim);
    // create simulation thread
    return new SimulationRunner(sim, renderer, agents);
  }

}
