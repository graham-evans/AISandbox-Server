/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mine;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.mine.proto.FlagAction;
import dev.aisandbox.server.simulation.mine.proto.MineAction;
import dev.aisandbox.server.simulation.mine.proto.MineState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockMineAgent extends MockAgent {

  @Getter
  private final String agentName;
  Random random = new Random();

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof MineState mineState) {
      getOutputQueue().add(MineAction.newBuilder().setX(random.nextInt(mineState.getWidth()))
          .setY(random.nextInt(mineState.getHeight()))
          .setAction(random.nextBoolean() ? FlagAction.DIG : FlagAction.PLACE_FLAG).build());
    }
  }

}
