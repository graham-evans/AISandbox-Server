package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.exception.SimulationException;

public class IllegalCoinAction extends SimulationException {

  public IllegalCoinAction(String message) {
    super(message);
  }

  public IllegalCoinAction(String message, Throwable cause) {
    super(message, cause);
  }
}
