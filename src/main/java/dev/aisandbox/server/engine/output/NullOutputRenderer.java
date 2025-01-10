package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;

public class NullOutputRenderer implements OutputRenderer{

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public void setup(Simulation simulation) {
        // do nothing
    }

    @Override
    public void display() {
        // do nothing;
    }

}
