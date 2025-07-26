/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.exception.SimulationException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

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
   * @param msg The Message to send
   * @throws SimulationException
   */
  @Override
  public void send(GeneratedMessage msg) throws SimulationException {
    // process message
  }

  @Override
  public <T extends GeneratedMessage> T receive(Class<T> responseType) throws SimulationException {
    if (outputQueue.isEmpty()) {
      throw new SimulationException("No response available");
    }
    GeneratedMessage message = outputQueue.removeFirst();
    if (message.getClass() != responseType) {
      throw new SimulationException(
          "Expected " + responseType + " but received " + message.getClass());
    } else {
      return (T) message;
    }
  }

  @Override
  public void close() {

  }
}
