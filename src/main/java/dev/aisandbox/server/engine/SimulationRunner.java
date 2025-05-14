/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SimulationRunner extends Thread {

  private final Simulation simulation;
  private final OutputRenderer outputRenderer;
  private final List<Agent> agents;

  private boolean running = true;

  public void stopSimulation() {
    running = false;
    this.interrupt();
  }

  @Override
  public void run() {
    log.info("Writing output to {}", outputRenderer.getName());
    log.info("Starting simulation...");
    // start simulation
    while (running) {
      try {
        simulation.step(outputRenderer);
      } catch (SimulationException e) {
        log.error(e.getMessage());
        running = false;
      }
    }
    // finish simulation
    simulation.close();
    agents.forEach(Agent::close);
  }

}
