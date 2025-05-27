/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockBanditPlayer implements Agent {

  private final Random random = new Random();

  @Getter
  private final String agentName;

  @Override
  public void send(GeneratedMessage o) {
    // do nothing
  }

  @Override
  public <T extends GeneratedMessage> T sendAndReceive(GeneratedMessage state, Class<T> responseType) {
    BanditState banditState = (BanditState) state;
    if (responseType != BanditAction.class) {
      log.error("Asking for {} but I can only respond with BanditAction", responseType.getName());
      return null;
    } else {
      return (T) BanditAction.newBuilder().setArm(random.nextInt(banditState.getBanditCount()))
          .build();
    }
  }

  @Override
  public void close() {

  }
}
