package dev.aisandbox.server.simulation.twisty.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Move {
  // move icons
  /**
   * Constant <code>MOVE_ICON_WIDTH=60</code>.
   */
  public static final int MOVE_ICON_WIDTH = 60;
  /**
   * Constant <code>MOVE_ICON_HEIGHT=100</code>.
   */
  public static final int MOVE_ICON_HEIGHT = 100;

  @Getter
  @Setter
  String name;
  @Getter
  List<MoveLoop> loops = new ArrayList<>();
  @Setter
  @Getter
  int cost;

  @Setter
  @Getter
  private BufferedImage imageIcon = new BufferedImage(MOVE_ICON_WIDTH, MOVE_ICON_HEIGHT,
      BufferedImage.TYPE_INT_RGB);

  @Override
  public String toString() {
    String sb = name + " (" + loops.size() + ")";
    return sb;
  }

  protected CompiledMove compileMove() {
 /*  CompiledMove cmove = new CompiledMove(cells.size());
    // copy move image
    cmove.setImage(move.getImageIcon());
    // copy move cost
    cmove.setCost(move.getCost());
    // setup matrix
    cmove.resetMove();
    // check we have loops
    if (move.getLoops().isEmpty()) {
      warnings.add("Move '" + move.getName() + "' has no loops");
    }
    // add each loop
    for (int i = 0; i < move.getLoops().size(); i++) {
      MoveLoop loop = move.getLoops().get(i);
      // check we have at least two cells
      if (loop.getCells().size() < 2) {
        warnings.add("Move '" + move.getName() + "' loop " + i
            + " has less than two cells - can't compile");
      } else {
        for (int j = 0; j < loop.getCells().size() - 1; j++) {
          cmove.setMatrixElement(cells.indexOf(loop.getCells().get(j + 1)),
              cells.indexOf(loop.getCells().get(j)));
        }
        cmove.setMatrixElement(cells.indexOf(loop.getCells().get(0)),
            cells.indexOf(loop.getCells().get(loop.getCells().size() - 1)));
      }
    }
  }*/
    return null;
  }
}
