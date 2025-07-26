/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.engine.exception.SimulationException;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameResult;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MockPlayer extends MockAgent {

  @Getter
  private final String agentName;

  Random rand = new Random();
  long messageCounter = 0;

  @Override
  public void send(GeneratedMessage o) throws SimulationException {
    if (o == null) {
      log.warn("{} received null object", agentName);
    } else {
      log.info("{} received object {}", agentName, o.getClass().getSimpleName());
    }
    if (messageCounter % 2 == 0) {
      // expect a CoinGameState and send an action
      CoinGameState state = (CoinGameState) o;
      // decode the state
      log.info("{} Creating random move from state {}", agentName, state.getCoinCountList());
      Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
      for (int row = 0; row < state.getCoinCountCount(); row++) {
        if (state.getCoinCount(row) > 0) {
          rowMap.put(row, state.getCoinCount(row));
        }
      }
      // pick a random row with some coins
      List<Map.Entry<Integer, Integer>> entryList = new ArrayList<Map.Entry<Integer, Integer>>(
          rowMap.entrySet());
      log.info("{} Filtered rows with items {}", agentName, entryList);
      Collections.shuffle(entryList, rand);
      Map.Entry<Integer, Integer> rowEntry = entryList.get(0);
      int takeCoins = Math.min(rand.nextInt(rowEntry.getValue()) + 1, state.getMaxPick());
      CoinGameAction action = CoinGameAction.newBuilder().setSelectedRow(rowEntry.getKey())
          .setRemoveCount(takeCoins).build();
      // make sure the queue is empty
      if (!getOutputQueue().isEmpty()) {
        log.error("Writing to non empty output queue");
        throw new SimulationException("Writing to non empty queue.");
      } else {
        log.info("{} sending action {}", agentName, action.toString().replaceAll("\n\r", ""));
        getOutputQueue().add(action);
      }
    } else {
      // expect a CoingGameResult
      CoinGameResult result = (CoinGameResult) o;
      log.info("{} recieved result {}", agentName, result.getStatus());
    }
    // advance the count
    messageCounter++;
  }

}
