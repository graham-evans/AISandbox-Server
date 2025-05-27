/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.highlowcards;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsState;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockPlayer extends MockAgent {

  private final Random random = new Random();

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof HighLowCardsState) {
      if (random.nextBoolean()) {
        getOutputQueue().add(HighLowCardsAction.newBuilder().setAction(HighLowChoice.HIGH).build());
      } else {
        getOutputQueue().add(HighLowCardsAction.newBuilder().setAction(HighLowChoice.LOW).build());
      }
    }
  }

}
