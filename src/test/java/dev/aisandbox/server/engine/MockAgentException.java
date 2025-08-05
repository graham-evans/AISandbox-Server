/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.exception.SimulationException;

/**
 * Exception thrown by mock agents during testing scenarios.
 * Extends SimulationException to provide specific error handling for mock agents.
 */
public class MockAgentException extends SimulationException {

  /**
   * Creates a new MockAgentException with the specified message.
   *
   * @param message the detail message explaining the exception
   */
  public MockAgentException(String message) {
    super(message);
  }
}
