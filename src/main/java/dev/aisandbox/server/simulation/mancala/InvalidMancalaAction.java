/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

/**
 * Exception thrown when an invalid action is attempted in the Mancala game.
 *
 * <p>This represents a move that would be legal in other game states but is not possible with the
 * current board configuration. For example, choosing a pit that is currently empty.
 *
 * <p>This will result in the player losing the game, but won't stop the simulation.
 */
public class InvalidMancalaAction extends SimulationException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new InvalidMancalaAction with the specified error message.
   *
   * @param message a descriptive error message
   */
  public InvalidMancalaAction(String message) {
    super(message);
  }

  /**
   * Creates a new InvalidMancalaAction with the specified error message and cause.
   *
   * @param message a descriptive error message
   * @param cause   the underlying cause of this exception
   */
  public InvalidMancalaAction(String message, Throwable cause) {
    super(message, cause);
  }
}
