/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.output;

import dev.aisandbox.server.engine.Simulation;

public class NullOutputRenderer implements OutputRenderer {

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

  @Override
  public void write(String text) {
    System.out.println(text);
  }
}
