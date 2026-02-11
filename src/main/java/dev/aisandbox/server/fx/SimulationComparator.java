/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import java.util.Comparator;

/**
 * Comparator for sorting SimulationBuilder objects by their simulation names.
 */
public class SimulationComparator implements Comparator<SimulationBuilder> {

  @Override
  public int compare(SimulationBuilder o1, SimulationBuilder o2) {
    return o1.getSimulationName().compareTo(o2.getSimulationName());
  }
}
