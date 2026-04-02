/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.proto.CascadeAction;
import dev.aisandbox.server.simulation.cascade.proto.CascadeState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock agent for testing the Cascade simulation.
 *
 * <p>On each turn the agent deserialises the board from the received {@link CascadeState},
 * enumerates all 112 possible adjacent swaps, filters them to those that produce at least one
 * match (valid moves), and picks one at random. This avoids wasting moves on illegal swaps while
 * keeping the agent simple.
 */
@Slf4j
@RequiredArgsConstructor
public class MockCascadeAgent extends MockAgent {

  @Getter
  private final String agentName;

  private final Random random = new Random();

  @Override
  public void send(GeneratedMessage msg) {
    if (!(msg instanceof CascadeState state)) {
      return;
    }

    // Reconstruct the board from the serialised rows
    CascadeBoard board = new CascadeBoard();
    CascadeBoardUtils.deserialiseBoard(board, state.getRowList());

    // Collect all valid swaps
    List<int[]> validSwaps = new ArrayList<>();

    // Horizontal pairs (x, x+1) for each row
    for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
      for (int x = 0; x < CascadeBoard.WIDTH - 1; x++) {
        if (CascadeBoardUtils.isValidSwap(board, x, y, x + 1, y)) {
          validSwaps.add(new int[]{x, y, x + 1, y});
        }
      }
    }

    // Vertical pairs (y, y+1) for each column
    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT - 1; y++) {
        if (CascadeBoardUtils.isValidSwap(board, x, y, x, y + 1)) {
          validSwaps.add(new int[]{x, y, x, y + 1});
        }
      }
    }

    int[] chosen;
    if (validSwaps.isEmpty()) {
      // No valid moves found (board will be reshuffled by the runtime); send a dummy swap
      log.warn("No valid swaps found — sending dummy action");
      chosen = new int[]{0, 0, 1, 0};
    } else {
      chosen = validSwaps.get(random.nextInt(validSwaps.size()));
    }

    getOutputQueue().add(CascadeAction.newBuilder()
        .setX1(chosen[0]).setY1(chosen[1])
        .setX2(chosen[2]).setY2(chosen[3])
        .build());
  }
}
