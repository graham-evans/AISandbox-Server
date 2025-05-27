/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.MockAgent;
import dev.aisandbox.server.simulation.maze.proto.MazeAction;
import dev.aisandbox.server.simulation.maze.proto.MazeResult;
import dev.aisandbox.server.simulation.maze.proto.MazeState;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QLearningMazeAgent extends MockAgent {

  @Getter
  private final String agentName;
  Random random = new Random();
  private String episodeID = null;
  private QLearningTable qLearningTable = null;
  private int mazeWidth = 0;
  private int mazeHeight = 0;

  @Override
  public void send(GeneratedMessage o) {
    if (o instanceof MazeResult mazeResult) {
      // Update Q-learning table based on the result
      int initialState = getState(mazeResult.getStartX(),
          mazeResult.getStartY()); // Assuming MazeState has a method to get state ID
      int action = mazeResult.getDirectionValue(); // Assuming MazeAction has a method to get
      // action value
      double reward = mazeResult.getStepScore(); // Assuming MazeResult has a method to get reward
      int nextState = getState(mazeResult.getEndX(), mazeResult.getEndY());
      qLearningTable.updateQValue(initialState, action, reward, nextState);
    } else if (o instanceof MazeState mazeState) {
      if (!mazeState.getEpisodeID().equals(episodeID)) {
        // initialize Q-learning table for the new episode
        mazeWidth = mazeState.getWidth();
        mazeHeight = mazeState.getHeight();
        episodeID = mazeState.getEpisodeID();
        qLearningTable = new QLearningTable(mazeState.getWidth() * mazeState.getHeight(),
            4); // Assuming 4 actions (N, S, E, W)
      }
      getOutputQueue().add(MazeAction.newBuilder().setDirectionValue(qLearningTable.chooseAction(
              getState(mazeState.getStartX(),
                  mazeState.getStartY()))) // we can do this as NSEW are 0,1,2,3
          .build());
    }
  }

  private int getState(int x, int y) {
    // Convert 2D coordinates to a single state ID
    return y * mazeWidth + x;
  }

  private class QLearningTable {

    private double[][] qTable; // Q-table to store Q-values
    private double learningRate; // Learning rate (alpha)
    private double discountFactor; // Discount factor (gamma)
    private double explorationRate; // Exploration rate (epsilon)

    public QLearningTable(int stateSize, int actionSize) {
      qTable = new double[stateSize][actionSize];
      learningRate = 0.1;
      discountFactor = 0.9;
      explorationRate = 0.1;
    }

    public void updateQValue(int state, int action, double reward, int nextState) {
      double bestNextQValue = getMaxQValue(nextState);
      qTable[state][action] +=
          learningRate * (reward + discountFactor * bestNextQValue - qTable[state][action]);
    }

    private double getMaxQValue(int state) {
      double maxQValue = qTable[state][0];
      for (int i = 1; i < qTable[state].length; i++) {
        if (qTable[state][i] > maxQValue) {
          maxQValue = qTable[state][i];
        }
      }
      return maxQValue;
    }

    public int chooseAction(int state) {
      if (random.nextDouble() < explorationRate) {
        return random.nextInt(qTable[state].length); // Explore
      } else {
        return getMaxAction(state); // Exploit
      }
    }

    private int getMaxAction(int state) {
      int maxAction = 0;
      for (int i = 1; i < qTable[state].length; i++) {
        if (qTable[state][i] > qTable[state][maxAction]) {
          maxAction = i;
        }
      }
      return maxAction;
    }
  }
}
  