/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

/**
 * Exception thrown when an illegal action is attempted in the Mancala game.
 *
 * <p>These actions are always illegal regardless of the game state and will result in the
 * simulation being stopped. For example:
 * <ul>
 *   <li>Selecting a pit index outside the valid range (0-5).</li>
 * </ul>
 */
public class IllegalMancalaAction extends SimulationException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new IllegalMancalaAction with the specified error message.
   *
   * @param message the detailed message describing the illegal action
   */
  public IllegalMancalaAction(String message) {
    super(message);
  }

  /**
   * Constructs a new IllegalMancalaAction with the specified error message and cause.
   *
   * @param message the detailed message describing the illegal action
   * @param cause   the underlying cause of this exception
   */
  public IllegalMancalaAction(String message, Throwable cause) {
    super(message, cause);
  }
}
