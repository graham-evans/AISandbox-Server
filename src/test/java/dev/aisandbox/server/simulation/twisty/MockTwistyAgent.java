/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.twisty.proto.TwistyAction;
import dev.aisandbox.server.simulation.twisty.proto.TwistyState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockTwistyAgent extends MockAgent {

  @Getter
  private final String agentName;
  Random random = new Random();

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof TwistyState twistyState) {
      getOutputQueue().add(TwistyAction.newBuilder()
          .setMove(twistyState.getValidMoves(random.nextInt(twistyState.getValidMovesCount())))
          .build());
    }
  }

}
