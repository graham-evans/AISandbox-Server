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

  public SimulationException(String message) {
    super(message);
  }

  public SimulationException(String message, Throwable cause) {
    super(message, cause);
  }
}
