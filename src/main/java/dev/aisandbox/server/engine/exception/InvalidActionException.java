/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

import java.io.Serial;

/**
 * Exception thrown when an agent attempts an action that violates a context-specific rule.
 *
 * <p>Unlike {@link IllegalActionException}, this exception does <em>not</em> terminate the
 * simulation. The simulation may apply a penalty and continue to the next step, or it may send feedback
 * and retry the step. Use this when the action is structurally well-formed but breaks a rule that only applies
 * in the current game state — for example, trying to move in a direction that is blocked,
 * or playing out of turn.
 *
 * <p>Example scenarios:
 * <ul>
 *   <li>In a maze simulation: attempting to move through a wall</li>
 *   <li>In a card game: playing a card that is not permitted this turn</li>
 *   <li>In a board game: placing a piece on a square that is currently occupied</li>
 * </ul>
 *
 * <p>Generally it is caught and processed within the {@link dev.aisandbox.server.engine.Simulation#step(dev.aisandbox.server.engine.output.OutputRenderer) Simulation#step()} method
 *
 * @see IllegalActionException
 * @see SimulationException
 */
public class InvalidActionException extends SimulationException {

  @Serial
  private static final long serialVersionUID = -2249273169805566707L;

  /**
   * Creates a new InvalidActionException with the specified error message.
   *
   * <p>The message should clearly describe what rule was violated.
   * It will be reported in the UI.
   *
   * @param message a descriptive error message explaining the invalid action
   */
  public InvalidActionException(String message) {
    super(message);
  }
}
