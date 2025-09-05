/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.network.NetworkAgent;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Core engine class that coordinates simulation execution with connected AI agents.
 *
 * <p>The SandboxEngine acts as the central coordinator between a simulation and its participating
 * AI agents. It manages the simulation lifecycle, handles agent communication, and orchestrates
 * the interaction between the simulation logic and external AI implementations.
 * </p>
 *
 * <p>This class encapsulates:
 * </p>
 * <ul>
 *   <li>The simulation configuration and builder</li>
 *   <li>The list of connected network agents</li>
 *   <li>Coordination of simulation execution flow</li>
 * </ul>
 *
 * <p>The engine is designed to work with any simulation that implements the {@link Simulation}
 * interface and can communicate with AI agents through the standardized protobuf protocol.
 * </p>
 *
 * @see SimulationBuilder
 * @see NetworkAgent
 * @see Simulation
 */
@RequiredArgsConstructor
public class SandboxEngine {

  /**
   * The simulation builder that defines the simulation configuration and can create simulation instances.
   */
  private final SimulationBuilder simulation;
  
  /**
   * The list of network agents participating in this simulation.
   */
  private final List<NetworkAgent> players;

}
