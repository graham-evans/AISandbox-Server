package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.GameException;

public class IllegalBanditAction extends GameException {

    public IllegalBanditAction(String message) {
        super(message);
    }

    public IllegalBanditAction(String message, Throwable cause) {
        super(message, cause);
    }
}
