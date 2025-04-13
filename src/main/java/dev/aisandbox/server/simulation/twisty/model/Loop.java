package dev.aisandbox.server.simulation.twisty.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class Loop {

  @Getter
  List<Cell> cells = new ArrayList<>();

  public void removeCell(Cell c) {
    cells.remove(c);
  }

  @Override
  public String toString() {
    if (cells.isEmpty()) {
      return "Empty Loop";
    } else {
      return "Loop with " + cells.size() + " cells";
    }
  }
}
