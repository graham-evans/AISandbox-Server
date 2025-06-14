/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

/**
 * Exception thrown when an illegal action is attempted in the coin game.
 * <p>
 * These actions are always illegal, regardless of the state of the game and will result in the
 * simulation being stopped. Such as:
 * <ul>
 *   <li>Attempting to take a negative, zero, or more than <i>MAX</i> number of coins.</li>
 *   <li>Trying to take coins from a pile that doesn't exist.</li>
 * </ul>
 * </p>
 */
public class IllegalCoinAction extends SimulationException {

  /**
   * Serial version UID for serialization
   */
  @Serial
  private static final long serialVersionUID = 5907213272446805507L;

  /**
   * Constructs a new IllegalCoinAction with the specified error message.
   *
   * @param message The detailed message describing the illegal action
   */
  public IllegalCoinAction(String message) {
    super(message);
  }

  /**
   * Constructs a new IllegalCoinAction with the specified error message and cause.
   *
   * @param message The detailed message describing the illegal action
   * @param cause   The underlying cause (which is saved for later retrieval by the
   *                {@link #getCause()} method)
   */
  public IllegalCoinAction(String message, Throwable cause) {
    super(message, cause);
  }
}
