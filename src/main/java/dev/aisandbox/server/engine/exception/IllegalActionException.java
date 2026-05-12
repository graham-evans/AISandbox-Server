/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

import java.io.Serial;

/**
 * Exception thrown when an agent attempts to perform an illegal action during simulation.
 *
 * <p>This exception is used to signal that an agent has requested an action that violates the
 * rules
 * of the current simulation and could **never** be a legal move. Common scenarios include:
 * <ul>
 *   <li>Moving to invalid coordinates or out-of-bounds positions</li>
 *   <li>Performing actions that violate game-specific rules</li>
 *   <li>Making requests with malformed or invalid parameters</li>
 * </ul>
 *
 * <p>When this exception is thrown, it always results in the simulation being terminated,
 * as the agent has demonstrated it cannot operate within the established constraints.
 * This helps ensure that AI agents are properly limited to respect
 * simulation boundaries.
 *
 * <p>This is separate from an {@link InvalidActionException}, which signals a context-specific
 * rule violation that may incur a penalty but allows the simulation to continue.
 *
 * @see SimulationRuntimeException
 */
public class IllegalActionException extends Exception {

  @Serial
  private static final long serialVersionUID = -8042532409067545774L;

  /**
   * Creates a new IllegalActionException with the specified error message.
   *
   * <p>The message should clearly describe what illegal action was attempted,
   * providing context for debugging and understanding why the simulation was terminated.
   *
   * @param message a descriptive error message explaining the illegal action
   */
  public IllegalActionException(String message) {
    super(message);
  }

  /**
   * Creates a new IllegalActionException with the specified error message and cause.
   *
   * @param message a descriptive error message explaining the illegal action
   * @param cause   the underlying cause of this exception
   */
  public IllegalActionException(String message, Throwable cause) {
    super(message, cause);
  }
}
