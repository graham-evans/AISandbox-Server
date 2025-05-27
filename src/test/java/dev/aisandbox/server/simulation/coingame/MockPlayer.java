/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
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

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof CoinGameState state) { // ignore all but the state object
      // decode the state
      log.info("Creating random move from state {}", state.getCoinCountList());
      Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
      for (int row = 0; row < state.getCoinCountCount(); row++) {
        if (state.getCoinCount(row) > 0) {
          rowMap.put(row, state.getCoinCount(row));
        }
      }
      // pick a random row with some coins
      List<Map.Entry<Integer, Integer>> entryList = new ArrayList<Map.Entry<Integer, Integer>>(
          rowMap.entrySet());
      log.info("Filtered rows with items {}", entryList);
      Collections.shuffle(entryList, rand);
      Map.Entry<Integer, Integer> rowEntry = entryList.get(0);
      int takeCoins = Math.min(rand.nextInt(rowEntry.getValue()) + 1, state.getMaxPick());
      getOutputQueue().add(
          CoinGameAction.newBuilder().setSelectedRow(rowEntry.getKey()).setRemoveCount(takeCoins)
              .build());
    }
  }

}
