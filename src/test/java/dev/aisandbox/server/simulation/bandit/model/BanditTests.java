/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BanditTests {

  public void rangeTest() {
    Bandit bandit = new Bandit(0.0, 1.0);
    assertEquals(0.0, bandit.getMean());
    assertEquals(1.0, bandit.getStd());
  }

}
