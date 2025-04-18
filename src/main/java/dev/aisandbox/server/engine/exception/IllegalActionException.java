package dev.aisandbox.server.engine.exception;

import java.io.Serial;

/**
 * Exception thrown when an agent requests something that doesn't make sense.
 * <p>
 * Will result in the simulation being aborted.
 */
public class IllegalActionException extends SimulationException {

  @Serial
  private static final long serialVersionUID = -8042532409067545774L;

  public IllegalActionException(String message) {
    super(message);
  }
}
