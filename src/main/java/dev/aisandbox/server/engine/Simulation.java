package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.output.OutputRenderer;

import java.awt.*;

public interface Simulation {
    default void close() {
        // no action
    }

    void step(OutputRenderer output);

    void visualise(Graphics2D graphics2D);
}
