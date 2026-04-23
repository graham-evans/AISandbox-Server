/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.exception.SimulationRuntimeException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Mock implementation of the Agent interface for testing purposes.
 * This agent can be programmed to send and receive specific messages during tests.
 */
public class MockAgent implements Agent {

  @Getter(AccessLevel.PROTECTED)
  List<GeneratedMessage> outputQueue = new ArrayList<>();

  @Override
  public String getAgentName() {
    return "Mock Agent";
  }

  /**
   * "Send" a message to the mock server, this may be followed by a receive request.
   *
   * <p>This is the method that needs overriding!
   *
   * @param msg The Message to send to the mock agent
   * @throws SimulationRuntimeException if there is an error processing the message
   */
  @Override
  public void send(GeneratedMessage msg) throws SimulationRuntimeException {
    // process message
  }

  @Override
  public <T extends GeneratedMessage> T receive(Class<T> responseType) throws SimulationRuntimeException {
    if (outputQueue.isEmpty()) {
      throw new SimulationRuntimeException("No response available");
    }
    GeneratedMessage message = outputQueue.removeFirst();
    if (message.getClass() != responseType) {
      throw new SimulationRuntimeException(
          "Expected " + responseType + " but received " + message.getClass());
    } else {
      return (T) message;
    }
  }

  @Override
  public void close() {

  }
}
