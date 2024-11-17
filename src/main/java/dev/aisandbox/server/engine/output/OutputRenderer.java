package dev.aisandbox.server.engine.output;

import java.io.File;

public interface OutputRenderer {
    public String getName();

    default void setup() {
        // do nothing
    }

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
