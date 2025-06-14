/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.exception.SimulationException;

/**
 * Exception thrown when an invalid action is attempted in the coin game.
 * <p>
 * This is a move that (in other circumstances) would be allowed, but is not possible with the game
 * in its current state.
 * <p>
 * This will result in the player losing the game, but won't result in the simulation stopping.
 * Possible actions include:
 * <ul>
 *   <li>Trying to take more coins from a pile than exist in that pile.</li>
 * </ul>
 * </p>
 */
public class InvalidCoinAction extends SimulationException {

  public InvalidCoinAction(String message) {
    super(message);
  }

  public InvalidCoinAction(String message, Throwable cause) {
    super(message, cause);
  }
}
