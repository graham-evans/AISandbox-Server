/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.exception;

public class SimulationSetupException extends Exception {

  private static final long serialVersionUID = -349185535858463388L;

  public SimulationSetupException(String message) {
    super(message);
  }
}
