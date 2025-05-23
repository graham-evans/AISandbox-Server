/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.output.OutputRenderer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SimulationSetup {

  public static SimulationRunner setupSimulation(SimulationBuilder builder, int agentCount,
      int defaultPort, OutputRenderer renderer) {
    AtomicInteger port = new AtomicInteger(defaultPort);
    List<Agent> agents = Arrays.stream(builder.getAgentNames(agentCount))
        .map(s -> (Agent) new NetworkAgent(s, port.getAndIncrement())).toList();
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
