/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

/**
 * Exception thrown when a simulation fails to set up or initialize properly.
 *
 * <p>This exception is raised when there are issues with simulation configuration,
 * parameter validation, or initialization that prevent the simulation from starting.
 */
public class SimulationSetupException extends Exception {

  private static final long serialVersionUID = -349185535858463388L;

  /**
   * Creates a simulation setup exception with the given message.
   *
   * @param message a descriptive message about the setup failure
   */
  public SimulationSetupException(String message) {
    super(message);
  }
}
