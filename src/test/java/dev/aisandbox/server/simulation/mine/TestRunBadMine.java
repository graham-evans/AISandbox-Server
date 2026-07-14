/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationRandomNumberGenerator;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.exception.IllegalActionException;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.telemetry.engine.NullTelemetryEngine;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Mine Hunter simulation - testing bad responses are dealt with.
 */
public class TestRunBadMine {

  /**
   * Test that sending out-of-bounds coordinates (Integer.MAX_VALUE) throws an
   * IllegalActionException rather than an unchecked ArrayIndexOutOfBoundsException.
   */
  @Test
  public void testRunBadHighMineGame() {
    assertThrows(IllegalActionException.class, () -> {
      MineHunterScenario builder = new MineHunterScenario();

      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new BadMineAgent(s, true))
          .toList();

      Simulation sim = builder.build(agents, Theme.LIGHT,
          new SimulationRandomNumberGenerator(0), new NullTelemetryEngine());

      OutputRenderer out = new NullOutputRenderer();
      out.setup(sim);

      for (int step = 0; step < 100; step++) {
        sim.step(out);
      }

      sim.close();
      agents.forEach(Agent::close);
    });
  }

  /**
   * Test that sending negative coordinates (Integer.MIN_VALUE) throws an
   * IllegalActionException rather than an unchecked ArrayIndexOutOfBoundsException.
   */
  @Test
  public void testRunBadLowMineGame() {
    assertThrows(IllegalActionException.class, () -> {
      MineHunterScenario builder = new MineHunterScenario();

      List<Agent> agents = Arrays.stream(builder.getAgentNames(1))
          .map(s -> (Agent) new BadMineAgent(s, false))
          .toList();

      Simulation sim = builder.build(agents, Theme.LIGHT,
          new SimulationRandomNumberGenerator(0), new NullTelemetryEngine());

      OutputRenderer out = new NullOutputRenderer();
      out.setup(sim);

      for (int step = 0; step < 100; step++) {
        sim.step(out);
      }

      sim.close();
      agents.forEach(Agent::close);
    });
  }

}
