package dev.aisandbox.server.engine;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BitmapOutputRenderer implements OutputRenderer {
    private final Simulation simulation;

    public void display() {
        simulation.visualise(null);
    }
}
