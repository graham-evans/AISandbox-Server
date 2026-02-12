/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.mancala;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.simulation.mancala.proto.MancalaAction;
import dev.aisandbox.server.simulation.mancala.proto.MancalaResult;
import dev.aisandbox.server.simulation.mancala.proto.MancalaState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock player for testing the Mancala simulation.
 */
@RequiredArgsConstructor
@Slf4j
public class MockMancalaPlayer extends MockAgent {

  @Getter
  private final String agentName;

  private final Random rand = new Random();
  private long messageCounter = 0;

  @Override
  public void send(GeneratedMessage o) throws SimulationException {
    if (o == null) {
      log.warn("{} received null object", agentName);
    } else {
      log.info("{} received object {}", agentName, o.getClass().getSimpleName());
    }
    if (messageCounter % 2 == 0) {
      // expect a MancalaState and send an action
      MancalaState state = (MancalaState) o;
      log.info("{} valid moves: {}", agentName, state.getValidMovesList());
      // pick a random valid move
      int moveIndex = rand.nextInt(state.getValidMovesCount());
      int selectedPit = state.getValidMoves(moveIndex);
      MancalaAction action = MancalaAction.newBuilder()
          .setSelectedPit(selectedPit)
          .build();
      if (!getOutputQueue().isEmpty()) {
        log.error("Writing to non empty output queue");
        throw new SimulationException("Writing to non empty queue.");
      } else {
        log.info("{} sending action: pit {}", agentName, selectedPit);
        getOutputQueue().add(action);
      }
    } else {
      // expect a MancalaResult
      MancalaResult result = (MancalaResult) o;
      log.info("{} received result {}", agentName, result.getSignal());
    }
    messageCounter++;
  }
}
