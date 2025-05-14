/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;
import java.io.File;

public interface OutputRenderer {

  String getName();

  void setup(Simulation simulation);

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
