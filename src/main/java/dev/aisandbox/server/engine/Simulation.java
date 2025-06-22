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
 */
public interface Simulation {

  /**
   * Finish the simulation, closing any resources as required.
   *
   * <p>Has a no-opp default.
   */
  default void close() {
    // no action
  }

  /**
   * Perform a step in the simulation.
   *
   * <p>Optionally trigger an update to the output (possibly more than once).</p>
   *
   * @param output the {@link OutputRenderer} to trigger updates.
   * @throws SimulationException an exception which denotes the simulation has failed and should be
   *                             shutdown.
   */
  void step(OutputRenderer output) throws SimulationException;

  /**
   * Visualise the current state of the simulation, called from a
   * {@link dev.aisandbox.server.engine.output.OutputRenderer}.
   *
   * <p>The {@link java.awt.Graphics2D} is for a display space with the same dimensions as an HD
   * Screen. Defined in {@link dev.aisandbox.server.engine.output.OutputConstants} as
   * {@link dev.aisandbox.server.engine.output.OutputConstants#HD_WIDTH} by
   * {@link dev.aisandbox.server.engine.output.OutputConstants#HD_HEIGHT}
   *
   * @param graphics2D the graphics object used to render the state.
   */
  void visualise(Graphics2D graphics2D);
}
