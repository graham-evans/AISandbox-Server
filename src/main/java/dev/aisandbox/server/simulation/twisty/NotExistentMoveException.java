package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

/**
 * NotExistentMoveException class.
 *
 * @author gde
 * @version $Id: $Id
 */
public class NotExistentMoveException extends SimulationException {

  @Serial
  private static final long serialVersionUID = 2581482828224322482L;

  /**
   * Constructor for NotExistentMoveException.
   *
   * @param message a {@link java.lang.String} object.
   */
  public NotExistentMoveException(String message) {
    super(message);
  }

  /**
   * Constructor for NotExistentMoveException.
   *
   * @param message a {@link java.lang.String} object.
   * @param cause   a {@link java.lang.Throwable} object.
   */
  public NotExistentMoveException(String message, Throwable cause) {
    super(message, cause);
  }
}
