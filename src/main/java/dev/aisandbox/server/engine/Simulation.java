/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.engine.output.OutputRenderer;
import java.awt.Graphics2D;

/**
 * Interface which all simulations must implement.
 *
 * <p>This interface defines the core contract for all AI Sandbox simulations. A simulation represents
 * a specific environment or game where AI agents can interact and be evaluated. Each simulation is
 * responsible for managing its own state, processing agent actions, and providing visual output.
 * </p>
 *
 * <p>Simulations operate in a step-based manner where each call to {@link #step(OutputRenderer)}
 * advances the simulation by one logical unit (turn, time step, etc.). The simulation should also
 * be able to render its current state to a graphics context for visualization.
 * </p>
 *
 * <p>Example implementations include card games, maze navigation, multi-armed bandits, and puzzle
 * solving scenarios.
 * </p>
 *
 * @see SimulationBuilder
 * @see SimulationRunner
 */
public interface Simulation {

  /**
   * Finish the simulation, closing any resources as required.
   *
   * <p>This method is called when the simulation is ending, either because it has completed naturally
   * or because it has been manually stopped. Implementations should clean up any resources such as
   * network connections, file handles, or background threads.
   * </p>
   *
   * <p>Has a no-op default implementation for simulations that don't require cleanup.
   * </p>
   */
  default void close() {
    // no action
  }

  /**
   * Perform a step in the simulation.
   *
   * <p>This method advances the simulation by one logical unit, which could be a turn, a time step, or
   * any other meaningful progression depending on the simulation type. During this step, the
   * simulation may:
   * </p>
   * <ul>
   *   <li>Send messages to agents requesting their next action</li>
   *   <li>Process agent responses and update the simulation state</li>
   *   <li>Apply game rules and check for win/loss conditions</li>
   *   <li>Trigger visual updates through the OutputRenderer</li>
   * </ul>
   *
   * <p>The method may call the OutputRenderer multiple times during a single step
   * to show intermediate states or animations.
   * </p>
   *
   * @param output the {@link OutputRenderer} to trigger visual updates
   * @throws SimulationException if an error occurs that requires the simulation to be stopped, such
   *                             as agent communication failures or invalid actions
   */
  void step(OutputRenderer output) throws SimulationException;

  /**
   * Visualise the current state of the simulation.
   *
   * <p>This method is called by an {@link OutputRenderer} to render the current state of the
   * simulation to a graphics context. The implementation should draw all relevant visual elements
   * including:
   * </p>
   * <ul>
   *   <li>Game board or environment</li>
   *   <li>Agent positions or states</li>
   *   <li>Score displays and statistics</li>
   *   <li>UI elements and controls</li>
   *   <li>Background and decorative elements</li>
   * </ul>
   *
   * <p>The graphics context represents an HD display surface with dimensions defined by
   * {@link dev.aisandbox.server.engine.output.OutputConstants#HD_WIDTH} by
   * {@link dev.aisandbox.server.engine.output.OutputConstants#HD_HEIGHT} (1920x1080 pixels).
   * </p>
   *
   * @param graphics2D the graphics object used to render the current state
   */
  void visualise(Graphics2D graphics2D);
}
