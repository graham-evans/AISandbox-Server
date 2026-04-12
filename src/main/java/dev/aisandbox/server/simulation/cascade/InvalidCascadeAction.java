package dev.aisandbox.server.simulation.cascade;

public class InvalidCascadeAction extends RuntimeException {
    public InvalidCascadeAction(String message) {
        super(message);
    }
}
