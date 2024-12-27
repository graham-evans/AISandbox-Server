package dev.aisandbox.server.engine;

import java.awt.*;

public interface Simulation {
    public void close();
    public void step(OutputRenderer output);
    public void visualise(Graphics2D graphics2D);
}
