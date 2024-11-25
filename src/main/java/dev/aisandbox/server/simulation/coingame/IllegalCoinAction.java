package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.GameException;

public class IllegalCoinAction extends GameException {

    public IllegalCoinAction(String message) {
        super(message);
    }

    public IllegalCoinAction(String message, Throwable cause) {
        super(message, cause);
    }
}
