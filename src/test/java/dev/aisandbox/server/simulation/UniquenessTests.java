/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.aisandbox.server.engine.SimulationBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests to verify uniqueness constraints on simulation builders. */
public class UniquenessTests {

  private static final List<SimulationBuilder> builderList = new ArrayList<>();

  @BeforeAll
  static void setUpBeforeClass() {
    Arrays.stream(SimulationEnumeration.values())
        .forEach(simulationEnum -> builderList.add(simulationEnum.getBuilder()));
  }

  /**
   * Tests that there are the expected number of simulation builders available.
   */
  @Test
  public void includedTest() {
    assertNotNull(builderList);
    assertEquals(7, builderList.size());
  }

  /**
   * Tests that all simulation builders have unique names.
   */
  @Test
  public void uniqueNameTest() {
    Set<String> names = new HashSet<>();
    builderList.forEach(builder -> {
      names.add(builder.getSimulationName().toLowerCase());
    });
    assertEquals(builderList.size(), names.size());
  }

  /**
   * Tests that no builders names have whitespace in them.
   */
  @Test
  public void whitespaceTest() {
    for (SimulationBuilder builder : builderList) {
      assertFalse(builder.getSimulationName().contains(" "),
          builder.getSimulationName() + " contains whitespace");
    }
  }
}
