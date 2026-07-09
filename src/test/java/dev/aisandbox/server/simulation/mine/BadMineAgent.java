/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.engine.MockAgentException;
import dev.aisandbox.server.simulation.mine.proto.FlagAction;
import dev.aisandbox.server.simulation.mine.proto.MineAction;
import dev.aisandbox.server.simulation.mine.proto.MineResult;
import dev.aisandbox.server.simulation.mine.proto.MineState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BadMineAgent - Responds with out-of-range coordinates to exercise bounds handling.
 */
@Slf4j
@RequiredArgsConstructor
public class BadMineAgent extends MockAgent {

  @Getter
  private final String agentName;
  private final boolean high;

  @Override
  public void send(GeneratedMessage o) throws MockAgentException {
    if (o instanceof MineResult) {
      // ignore results
    } else if (o instanceof MineState) {
      int badCoord = high ? Integer.MAX_VALUE : Integer.MIN_VALUE;
      getOutputQueue().add(MineAction.newBuilder()
          .setX(badCoord)
          .setY(0)
          .setAction(FlagAction.DIG)
          .build());
    } else {
      log.error("Unexpected message received: " + o);
      throw new MockAgentException("Unexpected message received: " + o);
    }
  }

}
