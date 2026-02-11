/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

import java.io.Serial;

/**
 * Generic simulation exception, used for all exceptions that occur during the execution of a
 * simulation.
 */
public class SimulationException extends Exception {

  @Serial
  private static final long serialVersionUID = -5614005125536427971L;

  /**
   * Creates a simulation exception with the given message.
   *
   * @param message a descriptive message about the exception
   */
  public SimulationException(String message) {
    super(message);
  }

  /**
   * Creates a simulation exception with the given message and cause.
   *
   * @param message a descriptive message about the exception
   * @param cause the underlying cause of this exception
   */
  public SimulationException(String message, Throwable cause) {
    super(message, cause);
  }
}
