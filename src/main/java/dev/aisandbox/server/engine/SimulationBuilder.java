/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import java.util.List;
import java.util.Random;

/**
 * Builder class for setting up simulations.
 *
 * <p>Any parameters that need to be configured for the simulation should be exposed as POJO
 * getters
 * and setters. In addition, the getParameters method should return their details so they can be
 * exposed via the UI and CLI.
 * </p>
 *
 * <p>SimulationBuilder implementations act as factories and configuration containers for
 * simulations.
 * They define the metadata about a simulation (name, description, parameter definitions) and
 * provide the logic to construct configured Simulation instances.
 * </p>
 *
 * <p>The builder pattern allows for flexible configuration of simulations through:
 * </p>
 * <ul>
 *   <li>Parameter validation and type safety</li>
 *   <li>Dynamic UI generation based on parameter definitions</li>
 *   <li>Default value management</li>
 *   <li>Agent count constraints</li>
 * </ul>
 *
 * @see Simulation
 * @see SimulationParameter
 */
public interface SimulationBuilder {

  /**
   * The name of the simulation (must not include whitespace).
   *
   * @return The name of the simulation
   */
  String getSimulationName();

  /**
   * A text description of the simulation.
   *
   * @return Descriptive text
   */
  String getDescription();

  /**
   * A list of parameters that can be set. These must map onto appropriate get and set methods.
   *
   * @return list of parameters [name,description,type].
   */
  List<SimulationParameter> getParameters();


  /**
   * The minimum number of agents that must be made available.
   *
   * @return number of agents
   */
  int getMinAgentCount();

  /**
   * The maximum number of agents that are allowed.
   *
   * @return number of agents
   */
  int getMaxAgentCount();

  /**
   * Create default agent names based on the number of players.
   *
   * <p>Generally these will just be "Agent 1", "Agent 2", "Agent 3"... but for simulations where
   * different agents perform different roles this can be used to differentiate between them and
   * make is obvious to the user which agent should be connected to which port. For example
   * "Dispatcher Agent", "Delivery Agent" etc.
   *
   * @param agentCount the number of agents to return names for (this will be between
   *                   {@link #getMinAgentCount()} and {@link #getMaxAgentCount()}).
   * @return an array of agent names.
   */
  String[] getAgentNames(int agentCount);

  /**
   * Build the {@link Simulation} object from the current settings.
   *
   * @param agents A list of player objects
   * @param theme  The theme for any visualisations.
   * @param random A random number generator.
   * @return The {@link Simulation} object which can be wrapped in a {@link SimulationRunner} or
   * stepped through manually.
   */
  Simulation build(List<Agent> agents, Theme theme, Random random);
}
