/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.exception.SimulationException;

/**
 * Interface representing an agent that can participate in simulations.
 *
 * <p>An agent provides a communication mechanism for sending and receiving Protocol Buffer
 * messages during simulation execution. This allows external AI implementations to interact with
 * simulations through a standardized messaging protocol.
 * </p>
 *
 * <p>Agents can be implemented as network-based agents that communicate over sockets, or other
 * types of agents depending on the specific needs of the simulation.
 * </p>
 *
 * @see dev.aisandbox.server.engine.network.NetworkAgent
 */
public interface Agent {

  /**
   * Get the agents name (usually assigned by the SimulationBuilder).
   *
   * @return The name of the agent.
   */
  String getAgentName();

  /**
   * Send a message to the agent.
   *
   * <p>This method transmits a Protocol Buffer message to the connected agent. The message format
   * depends on the specific simulation and the current game state.
   * </p>
   *
   * @param msg The Protocol Buffer message to send
   * @throws SimulationException Can be thrown on IO error or communication failure.
   */
  void send(GeneratedMessage msg) throws SimulationException;

  /**
   * Wait (block) until the agent responds with the expected object.
   *
   * <p>This method blocks the current thread until the agent sends back a response of the
   * specified type. If the agent sends an unexpected message type or disconnects, an exception will
   * be thrown.
   * </p>
   *
   * @param responseType The class of the message to be returned (must extend GeneratedMessage).
   * @param <T>          The message type to be returned.
   * @return The agents response message.
   * @throws SimulationException Can be thrown if the wrong object is returned, if the agent
   *                             disconnects, or if there are communication errors.
   */
  <T extends GeneratedMessage> T receive(Class<T> responseType) throws SimulationException;

  /**
   * Close the agent connection and clean up any resources.
   *
   * <p>This method should be called when the simulation ends or when the agent is no longer
   * needed. It ensures proper cleanup of network connections, threads, and other resources.
   * </p>
   */
  void close();
}
