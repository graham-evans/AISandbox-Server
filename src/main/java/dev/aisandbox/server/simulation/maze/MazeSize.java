package dev.aisandbox.server.simulation.maze;

import lombok.Getter;

public enum MazeSize {
  SMALL(8, 6, 5),
  MEDIUM(20, 15, 2),
  LARGE(40, 30, 1);

  @Getter
  private final int width;
  @Getter
  private final int height;
  @Getter
  private final int zoomLevel;

  MazeSize(int width, int height, int zoomLevel) {
    this.width = width;
    this.height = height;
    this.zoomLevel = zoomLevel;
  }

}
