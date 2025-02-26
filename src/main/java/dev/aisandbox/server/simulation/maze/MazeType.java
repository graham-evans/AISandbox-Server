package dev.aisandbox.server.simulation.maze;

public enum MazeType {
    BINARYTREE("Binary Tree (Biased)"),
    SIDEWINDER("Sidewinder"),
    RECURSIVEBACKTRACKER("Recursive Backtracker"),
    BRAIDED("Braided (includes loops)");

    private final String name;

    private MazeType(final String name) {
        this.name = name;
    }

}
