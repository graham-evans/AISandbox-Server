/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.coingame;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
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
public class MockPlayer implements Agent {

  @Getter
  private final String agentName;

  Random rand = new Random();

  @Override
  public void send(GeneratedMessage o) {
    // ignore send messages
  }

  @Override
  public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
    if (responseType != CoinGameAction.class) {
      log.error("Asking for {} but I can only respond with CoinGameAction", responseType.getName());
      return null;
    } else {
      // decode the state
      CoinGameState cState = (CoinGameState) state;
      log.info("Creating random move from state {}", cState.getCoinCountList());
      Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
      for (int row = 0; row < cState.getCoinCountCount(); row++) {
        if (cState.getCoinCount(row) > 0) {
          rowMap.put(row, cState.getCoinCount(row));
        }
      }
      // pick a random row with some coins
      List<Map.Entry<Integer, Integer>> entryList = new ArrayList<Map.Entry<Integer, Integer>>(
          rowMap.entrySet());
      log.info("Filtered rows with items {}", entryList);
      Collections.shuffle(entryList, rand);
      Map.Entry<Integer, Integer> rowEntry = entryList.get(0);
      int takeCoins = Math.min(rand.nextInt(rowEntry.getValue()) + 1, cState.getMaxPick());
      return (T) CoinGameAction.newBuilder().setSelectedRow(rowEntry.getKey())
          .setRemoveCount(takeCoins).build();
    }
  }

  @Override
  public void close() {

  }
}
