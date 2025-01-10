package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;

import java.io.File;

public interface OutputRenderer {
    public String getName();

    public void setup(Simulation simulation);

    default void setSkipFrames(int framesToSkip) {
        // do nothing
    }

    default void setOutputDirectory(File outputDirectory) {
        // do nothing
    }

    void display();

    default void close() {
        // do nothing
    }

}
