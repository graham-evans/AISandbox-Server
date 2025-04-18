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
