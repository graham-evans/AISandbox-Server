/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

/**
 * Exception thrown when an attempt is made to execute a move that doesn't exist for a twisty puzzle.
 * <p>
 * This exception is used in twisty puzzle simulations (such as Rubik's Cube or similar puzzles)
 * when a client requests an invalid or non-existent move operation. For example, attempting
 * to perform a move that is not defined for the current puzzle configuration.
 * </p>
 *
 * @author gde
 * @version $Id: $Id
 * @see dev.aisandbox.server.engine.exception.SimulationException
 */
public class NotExistentMoveException extends SimulationException {

  /**
   * Serialization version UID for maintaining compatibility across different versions.
   */
  @Serial
  private static final long serialVersionUID = 2581482828224322482L;

  /**
   * Constructs a new NotExistentMoveException with the specified error message.
   * <p>
   * This constructor should be used when only an error message is needed
   * without specifying an underlying cause.
   * </p>
   *
   * @param message a {@link java.lang.String} containing the description of the error
   */
  public NotExistentMoveException(String message) {
    super(message);
  }

  /**
   * Constructs a new NotExistentMoveException with the specified error message and cause.
   * <p>
   * This constructor should be used when both an error message and the underlying
   * exception that caused this exception are needed.
   * </p>
   *
   * @param message a {@link java.lang.String} containing the description of the error
   * @param cause   a {@link java.lang.Throwable} object representing the underlying cause of this exception
   */
  public NotExistentMoveException(String message, Throwable cause) {
    super(message, cause);
  }
}
