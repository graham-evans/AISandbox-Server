/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

import java.io.Serial;

/**
 * Exception thrown when an agent attempts to perform an invalid or illegal action during simulation.
 * <p>
 * This exception is used to signal that an agent has requested an action that violates the rules
 * of the current simulation or is contextually inappropriate. Common scenarios include:
 * </p>
 * <ul>
 *   <li>Moving to invalid coordinates or out-of-bounds positions</li>
 *   <li>Attempting actions when the agent is in an invalid state</li>
 *   <li>Performing actions that violate game-specific rules</li>
 *   <li>Making requests with malformed or invalid parameters</li>
 * </ul>
 * <p>
 * When this exception is thrown, it typically results in the simulation being terminated,
 * as the agent has demonstrated it cannot operate within the established constraints.
 * This helps ensure that AI agents are properly trained to understand and respect
 * simulation boundaries.
 * </p>
 * <p>
 * Example scenarios where this might be thrown:
 * </p>
 * <ul>
 *   <li>In a maze simulation: attempting to move through walls</li>
 *   <li>In a card game: playing cards not in the agent's hand</li>
 *   <li>In a board game: placing pieces on occupied squares</li>
 * </ul>
 *
 * @see SimulationException
 */
public class IllegalActionException extends SimulationException {

  @Serial
  private static final long serialVersionUID = -8042532409067545774L;

  /**
   * Creates a new IllegalActionException with the specified error message.
   * <p>
   * The message should clearly describe what illegal action was attempted,
   * providing context for debugging and understanding why the simulation
   * was terminated.
   * </p>
   *
   * @param message a descriptive error message explaining the illegal action
   */
  public IllegalActionException(String message) {
    super(message);
  }
}
