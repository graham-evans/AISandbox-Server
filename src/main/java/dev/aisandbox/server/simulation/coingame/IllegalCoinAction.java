/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.exception.SimulationException;
import java.io.Serial;

public class IllegalCoinAction extends SimulationException {

  @Serial
  private static final long serialVersionUID = 5907213272446805507L;

  public IllegalCoinAction(String message) {
    super(message);
  }

  public IllegalCoinAction(String message, Throwable cause) {
    super(message, cause);
  }
}
