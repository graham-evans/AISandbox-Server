/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import java.awt.image.BufferedImage;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a compiled move for a twisty puzzle.
 * <p>
 * This class defines a move as a permutation matrix that can be applied to puzzle states. Each move
 * transforms the puzzle state by rearranging elements according to the defined matrix. The moves
 * can be visualized with an associated image and have an assigned cost.
 * </p>
 */
public class CompiledMove {

  /**
   * The number of elements in the puzzle state that this move affects.
   */
  private final int cardinality;

  /**
   * The permutation matrix representing this move.
   * <p>
   * Each value matrix[i] = j means that the element at position i in the new state should be the
   * element that was at position j in the original state.
   * </p>
   */
  private final int[] matrix;

  /**
   * The image representing this move for visualization purposes.
   */
  @Getter
  @Setter
  BufferedImage image;

  /**
   * The cost associated with performing this move.
   */
  @Getter
  @Setter
  int cost;

  /**
   * Constructs a new CompiledMove with the specified cardinality.
   *
   * @param cardinality the number of elements in the puzzle state
   */
  public CompiledMove(int cardinality) {
    this.cardinality = cardinality;
    matrix = new int[cardinality];
  }

  /**
   * Resets the move to the identity permutation.
   * <p>
   * After calling this method, applying the move will not change the state.
   * </p>
   */
  protected void resetMove() {
    for (int i = 0; i < cardinality; i++) {
      matrix[i] = i;
    }
  }

  /**
   * Sets a specific element in the permutation matrix.
   *
   * @param index the position in the matrix to set
   * @param value the new value for that position
   */
  protected void setMatrixElement(int index, int value) {
    matrix[index] = value;
  }

  /**
   * Gets a specific element from the permutation matrix.
   *
   * @param index the position in the matrix to query
   * @return the value at the specified position
   */
  protected int getMatrixElement(int index) {
    return matrix[index];
  }

  /**
   * Applies this move to a puzzle state.
   * <p>
   * The state is represented as a string, where each character represents a specific element of the
   * puzzle. The move rearranges these elements according to the permutation matrix.
   * </p>
   *
   * @param state the current puzzle state as a string
   * @return the new puzzle state after applying this move
   */
  public String applyMove(String state) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cardinality; i++) {
      sb.append(state.charAt(matrix[i]));
    }
    return sb.toString();
  }
}
