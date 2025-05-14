/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a move that can be applied to a twisty puzzle. A move consists of one or more loops of
 * cells that rotate together. Each move has a name, an associated cost, and an image icon for
 * visual representation. The move can be compiled * into a single operation for efficiency.
 */
@Slf4j
public class Move {
  // move icons
  /**
   * The standard width of a move icon in pixels. This constant defines the width for visual
   * representations of moves.
   */
  public static final int MOVE_ICON_WIDTH = 60;

  /**
   * The standard height of a move icon in pixels. This constant defines the height for visual
   * representations of moves.
   */
  public static final int MOVE_ICON_HEIGHT = 85;

  /**
   * The name of the move, typically representing its notation.
   */
  @Getter
  @Setter
  String name;

  /**
   * The list of move loops that define which cells move together during this move. Each move loop
   * represents a cycle of cells that rotate as a unit.
   */
  @Getter
  List<MoveLoop> loops = new ArrayList<>();

  /**
   * The cost associated with performing this move. Used for optimizing move sequences and
   * calculating puzzle solutions.
   */
  @Setter
  @Getter
  int cost;

  /**
   * The image icon representing this move visually. Default is an empty RGB image with dimensions
   * specified by MOVE_ICON_WIDTH and MOVE_ICON_HEIGHT.
   */
  @Setter
  @Getter
  private BufferedImage imageIcon = new BufferedImage(MOVE_ICON_WIDTH, MOVE_ICON_HEIGHT,
      BufferedImage.TYPE_INT_RGB);

  /**
   * Returns a string representation of this move. The representation includes the move's name and
   * the number of loops it contains.
   *
   * @return A string describing the move
   */
  @Override
  public String toString() {
    return name + " (" + loops.size() + ")";
  }

  /**
   * Compiles this move into a CompiledMove object that can be efficiently applied to a puzzle. The
   * compilation process converts the loop-based representation into a matrix-based representation
   * that specifies how cells should be transformed.
   *
   * @param puzzle The twisty puzzle that this move will be applied to
   * @return A compiled move ready for application to the puzzle
   * @throws IllegalArgumentException if the move doesn't contain any loops
   */
  protected CompiledMove compileMove(TwistyPuzzle puzzle) {
    List<Cell> cells = puzzle.getCells();
    CompiledMove cmove = new CompiledMove(cells.size());
    // copy move image
    cmove.setImage(imageIcon);
    // copy move cost
    cmove.setCost(cost);
    // setup matrix
    cmove.resetMove();
    // check we have loops
    if (loops.isEmpty()) {
      log.warn("No loops found in move {}", name);
      throw new IllegalArgumentException("No loops found in move " + name);
    }
    // add each loop
    for (MoveLoop loop : loops) {
      // check we have at least two cells
      if (loop.getCells().size() < 2) {
        log.warn("Loop {} has less than two cells", loop);
      } else {
        for (int j = 0; j < loop.getCells().size() - 1; j++) {
          cmove.setMatrixElement(cells.indexOf(loop.getCells().get(j + 1)),
              cells.indexOf(loop.getCells().get(j)));
        }
        cmove.setMatrixElement(cells.indexOf(loop.getCells().getFirst()),
            cells.indexOf(loop.getCells().getLast()));
      }
    }
    return cmove;
  }
}
