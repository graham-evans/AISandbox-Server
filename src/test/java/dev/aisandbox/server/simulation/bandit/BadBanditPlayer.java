/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.bandit;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.engine.MockAgentException;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditResult;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BadBandit Player - Responds with out-of-range bandits to pull.
 */
@Slf4j
@RequiredArgsConstructor
public class BadBanditPlayer extends MockAgent {

  private final String banditName;
  private final boolean high;

  @Override
  public void send(GeneratedMessage o) throws MockAgentException {
    if (o instanceof BanditResult) {
      // do nothing - we ignore the results.
    } else if (o instanceof BanditState state) {
      if (high) {
        getOutputQueue().add(BanditAction.newBuilder().setArm(Integer.MAX_VALUE).build());
      } else {
        getOutputQueue().add(BanditAction.newBuilder().setArm(Integer.MIN_VALUE).build());
      }
    } else {
      log.error("Unexpected message received: " + o);
      throw new MockAgentException("Unexpected message received: " + o);
    }
  }

}

