/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.highlowcards;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockPlayer implements Agent {

  private final Random random = new Random();

  @Override
  public String getAgentName() {
    return "Mock Player";
  }

  @Override
  public void send(GeneratedMessage o) {
    // send message to player - ignore it
  }

  @Override
  public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
    if (responseType != HighLowCardsAction.class) {
      log.error("Asking for {} but I can only respond with HighLowCardAction",
          responseType.getName());
      return null;
    } else {
      if (random.nextBoolean()) {
        return (T) HighLowCardsAction.newBuilder().setAction(HighLowChoice.HIGH).build();
      } else {
        return (T) HighLowCardsAction.newBuilder().setAction(HighLowChoice.LOW).build();
      }
    }
  }

  @Override
  public void close() {

  }
}
