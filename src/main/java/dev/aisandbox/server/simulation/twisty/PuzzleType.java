package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PuzzleType class.
 *
 * @author gde
 * @version $Id: $Id
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PuzzleType {
  CUBE3("Cube 3x3x3", "Cube 3x3x3 (OBTM)"), CUBE2("Cube 2x2x2", "Cube 2x2x2 (OBTM)"), CUBE4(
      "Cube 4x4x4", "Cube 4x4x4 (OBTM)"), CUBE5("Cube 5x5x5", "Cube 5x5x5 (OBTM)"), CUBE6(
      "Cube 6x6x6", "Cube 6x6x6 (OBTM)"), CUBE7("Cube 7x7x7", "Cube 7x7x7 (OBTM)"), CUBE8(
      "Cube 8x8x8", "Cube 8x8x8 (OBTM)"), CUBE9("Cube 10x10x10", "Cube 10x10x10 (OBTM)"), CUBE223(
      "Cuboid 2x2x3", "Cuboid 2x2x3 (OBTM)"), CUBE224("Cuboid 2x2x4",
      "Cuboid 2x2x4 (OBTM)"), CUBE225("Cuboid 2x2x5", "Cuboid 2x2x5 (OBTM)"), CUBE226(
      "Cuboid 2x2x6", "Cuboid 2x2x6 (OBTM)"), CUBE332("Cuboid 3x3x2",
      "Cuboid 3x3x2 (OBTM)"), CUBE334("Cuboid 3x3x4", "Cuboid 3x3x4 (OBTM)"), CUBE335(
      "Cuboid 3x3x5", "Cuboid 3x3x5 (OBTM)");
//  PYRAMID3("Pyramid 3", "Pyramid 3");


  private final String name;
  private final String id;

  public String toString() {
    return name;
  }

  public String getID() {
    return id;
  }

  public TwistyPuzzle getTwistyPuzzle() throws IOException {
    return switch (this) {
      case CUBE3 -> CuboidBuilder.buildCuboid(3, 3, 3);
      case CUBE2 -> CuboidBuilder.buildCuboid(2, 2, 2);
      case CUBE4 -> CuboidBuilder.buildCuboid(4, 4, 4);
      case CUBE5 -> CuboidBuilder.buildCuboid(5, 5, 5);
      case CUBE6 -> CuboidBuilder.buildCuboid(6, 6, 6);
      case CUBE7 -> CuboidBuilder.buildCuboid(7, 7, 7);
      case CUBE8 -> CuboidBuilder.buildCuboid(8, 8, 8);
      case CUBE9 -> CuboidBuilder.buildCuboid(9, 9, 9);
      case CUBE223 -> CuboidBuilder.buildCuboid(2, 2, 3);
      case CUBE224 -> CuboidBuilder.buildCuboid(2, 2, 4);
      case CUBE225 -> CuboidBuilder.buildCuboid(2, 2, 5);
      case CUBE226 -> CuboidBuilder.buildCuboid(2, 2, 6);
      case CUBE332 -> CuboidBuilder.buildCuboid(3, 3, 2);
      case CUBE334 -> CuboidBuilder.buildCuboid(3, 3, 4);
      case CUBE335 -> CuboidBuilder.buildCuboid(3, 3, 5);
    };
  }
}
