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

/**
 * Thread responsible for running a simulation.
 * Manages the lifecycle of a simulation including stepping through the simulation,
 * handling exceptions, and cleaning up resources when finished.
 */
@Slf4j
@RequiredArgsConstructor
public class SimulationRunner extends Thread {

  /** The simulation to be executed. */
  private final Simulation simulation;
  
  /** Renderer used to visualize the simulation state. */
  private final OutputRenderer outputRenderer;
  
  /** List of agents participating in the simulation. */
  private final List<Agent> agents;

  /** Flag to control the simulation loop. */
  private boolean running = true;

  /**
   * Stops the simulation safely.
   * Sets the running flag to false and interrupts the thread to ensure
   * timely termination of long-running or blocked operations.
   */
  public void stopSimulation() {
    running = false;
    this.interrupt();
  }

  /**
   * Main execution method of the simulation thread.
   * Continuously steps through the simulation until completion or interruption.
   * Handles any simulation exceptions and performs cleanup when finished.
   */
  @Override
  public void run() {
    log.info("Writing output to {}", outputRenderer.getName());
    log.info("Starting simulation...");
    
    // Main simulation loop - continuously steps through the simulation
    // until explicitly stopped or an exception occurs
    while (running) {
      try {
        simulation.step(outputRenderer);
      } catch (SimulationException e) {
        log.error(e.getMessage());
        running = false;
      }
    }
    
    // Cleanup phase - close the simulation and all agent connections
    // to ensure proper resource management
    log.info("Simulation ended, cleaning up resources");
    simulation.close();
    agents.forEach(Agent::close);
  }

}
