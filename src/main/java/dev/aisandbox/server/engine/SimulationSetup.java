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

@UtilityClass
public class SimulationSetup {

  public static SimulationRunner setupSimulation(SimulationBuilder builder, int agentCount,
      int defaultPort, OutputRenderer renderer) throws SimulationSetupException {
    AtomicInteger port = new AtomicInteger(defaultPort);
    String[] agentNames = builder.getAgentNames(agentCount);
    List<Agent> agents = new ArrayList<>();
    for (String agentName : agentNames) {
      agents.add(new NetworkAgent(agentName, port.getAndIncrement()));
    }
    return setupSimulation(builder, agents, renderer);
  }

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
