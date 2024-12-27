package dev.aisandbox.server.engine.output;

public interface OutputRenderer {
    public String getName();

    default void setup() {
        // do nothing
    }

    void display();

    default void close() {
        // do nothing
    }

}
