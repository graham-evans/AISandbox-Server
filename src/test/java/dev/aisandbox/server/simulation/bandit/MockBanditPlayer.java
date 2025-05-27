/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockBanditPlayer extends MockAgent {

  private final Random random = new Random();

  private final String name;

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof BanditState state) { // only respond to state objects
      getOutputQueue().add(
          BanditAction.newBuilder().setArm(random.nextInt(state.getBanditCount())).build());
    }
  }

}
