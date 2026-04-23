/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;

import java.util.List;
import java.util.Random;

/**
 * Simulation builder for the Cascade match-3 puzzle.
 *
 * <p>Cascade is a turn-based match-3 simulation in which a single agent scores points by swapping
 * adjacent tiles on an 8×8 grid to form matching groups, trigger chain reactions, and detonate
 * special objects. A standard game lasts 30 moves; the goal is to maximise the total score.
 *
 * <p>This builder has no configurable parameters — the board size and move budget are fixed.
 */
public final class CascadeScenario implements SimulationBuilder {

  /**
   * Returns the name of this simulation.
   *
   * @return {@code "Cascade"}
   */
  @Override
  public String getSimulationName() {
    return "Cascade";
  }

  /**
   * Returns a short description of the Cascade simulation.
   *
   * @return descriptive text suitable for display in the UI
   */
  @Override
  public String getDescription() {
    return "Score as many points as possible within 30 moves by swapping adjacent tiles to form "
        + "matching groups, trigger chain reactions, and detonate special objects on an 8x8 grid "
        + "of coloured tiles.";
  }

  /**
   * Returns the list of configurable parameters for this simulation.
   *
   * <p>Cascade has no configurable parameters.
   *
   * @return an empty list
   */
  @Override
  public List<SimulationParameter> getParameters() {
    return List.of();
  }

  /**
   * Returns the minimum number of agents required.
   *
   * @return {@code 1}
   */
  @Override
  public int getMinAgentCount() {
    return 1;
  }

  /**
   * Returns the maximum number of agents supported.
   *
   * @return {@code 1}
   */
  @Override
  public int getMaxAgentCount() {
    return 1;
  }

  /**
   * Returns the default agent name for this simulation.
   *
   * @param agentCount the number of agents (always {@code 1} for Cascade)
   * @return an array containing the single agent name
   */
  @Override
  public String[] getAgentNames(int agentCount) {
    return new String[]{"Agent 1"};
  }

  /**
   * Constructs a new {@link CascadeRuntime} using the provided agent, theme, and random source.
   *
   * @param agents          the list of agents (only the first is used)
   * @param theme           the visual theme for rendering
   * @param random          the source of randomness for board generation and tile refill
   * @param telemetryEngine
   * @return a new {@link CascadeRuntime} instance ready for play
   */
  @Override
  public Simulation build(List<Agent> agents, Theme theme, Random random, TelemetryEngine telemetryEngine) {
    return new CascadeRuntime(agents.getFirst(), theme, random);
  }
}
