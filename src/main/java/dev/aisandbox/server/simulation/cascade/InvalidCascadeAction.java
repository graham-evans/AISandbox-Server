/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.engine.exception.InvalidActionException;
import java.io.Serial;

/**
 * Exception thrown when an agent attempts an action that violates a context-specific rule
 * in the Cascade simulation.
 *
 * <p>This exception does not terminate the simulation — the move is counted as wasted and
 * play continues. Thrown by {@link CascadeBoardUtils#makeMove} in three cases:
 * <ul>
 *   <li>The two cells are not adjacent (Manhattan distance != 1)</li>
 *   <li>One or both cells are not swappable (ICE, STONE, or EMPTY)</li>
 *   <li>The swap would create no match</li>
 * </ul>
 *
 * @see InvalidActionException
 */
public class InvalidCascadeAction extends InvalidActionException {

  @Serial
  private static final long serialVersionUID = -5074848933343083895L;

  /**
   * Creates a new InvalidCascadeAction with the specified error message.
   *
   * @param message a descriptive error message explaining the invalid action
   */
  public InvalidCascadeAction(String message) {
    super(message);
  }
}
