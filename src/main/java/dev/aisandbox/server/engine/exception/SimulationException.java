package dev.aisandbox.server.engine.exception;

/**
 * Generic simulation exception, used for all exceptions that occur during the execution of a
 * simulation.
 */
public class SimulationException extends Exception {

  public SimulationException(String message) {
    super(message);
  }

  public SimulationException(String message, Throwable cause) {
    super(message, cause);
  }
}
