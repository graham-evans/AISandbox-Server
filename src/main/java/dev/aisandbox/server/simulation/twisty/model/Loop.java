package dev.aisandbox.server.simulation.twisty.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
