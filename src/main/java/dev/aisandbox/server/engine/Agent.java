/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.exception.SimulationException;

public interface Agent {

  /**
   * Get the agents name (usually assigned by the SimulationBuilder.
   *
   * @return The name of the agent.
   */
  String getAgentName();

  /**
   * Send a message to the agent.
   *
   * @param msg The Message to send
   * @throws SimulationException Can be thrown on IO error.
   */
  void send(GeneratedMessage msg) throws SimulationException;

  /**
   * Wait (block) until the agent responds with the expected object.
   *
   * @param responseType The class of the message to be returned (must extend GeneratedMessage).
   * @param <T>          The message type to be returned.
   * @return The agents message.
   * @throws SimulationException Can be thrown if the wrong object is returned or if they
   *                             disconnect.
   */
  <T extends GeneratedMessage> T receive(Class<T> responseType) throws SimulationException;

  void close();
}
