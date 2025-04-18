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
