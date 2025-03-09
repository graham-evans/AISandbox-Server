package dev.aisandbox.server.engine.exception;

/**
 * Exception thrown when an agent requests something that doesn't make sense.
 * <p>
 * Will result in the simulation being aborted.
 */
public class IllegalActionException extends SimulationException {
    public IllegalActionException(String message) {
        super(message);
    }
}
