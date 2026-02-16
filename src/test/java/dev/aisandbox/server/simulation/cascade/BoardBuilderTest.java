/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import dev.aisandbox.server.simulation.cascade.model.TileColour;
import dev.aisandbox.server.simulation.cascade.model.TileType;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests that {@link CascadeBoardBuilder} correctly parses every recognised board character. */
public class BoardBuilderTest {

  /**
   * Provides one argument set per recognised character: the character, the expected
   * {@link TileType}, and the expected {@link TileColour}.
   *
   * @return stream of arguments covering all 16 board characters
   */
  static Stream<Arguments> allBoardCharacters() {
    return Stream.of(
        // Standard coloured tiles
        Arguments.of('R', TileType.STANDARD, TileColour.RED),
        Arguments.of('B', TileType.STANDARD, TileColour.BLUE),
        Arguments.of('G', TileType.STANDARD, TileColour.GREEN),
        Arguments.of('Y', TileType.STANDARD, TileColour.YELLOW),
        Arguments.of('P', TileType.STANDARD, TileColour.PURPLE),
        // Ice-encased coloured tiles
        Arguments.of('r', TileType.ICE, TileColour.RED),
        Arguments.of('b', TileType.ICE, TileColour.BLUE),
        Arguments.of('g', TileType.ICE, TileColour.GREEN),
        Arguments.of('y', TileType.ICE, TileColour.YELLOW),
        Arguments.of('p', TileType.ICE, TileColour.PURPLE),
        // Special objects
        Arguments.of('*', TileType.BOMB,     TileColour.NONE),
        Arguments.of('H', TileType.ROCKET_H, TileColour.NONE),
        Arguments.of('V', TileType.ROCKET_V, TileColour.NONE),
        Arguments.of('~', TileType.PRISM,    TileColour.NONE),
        Arguments.of('S', TileType.STONE,    TileColour.NONE),
        // Empty
        Arguments.of('.', TileType.EMPTY,    TileColour.NONE)
    );
  }

  /**
   * Fills the entire 8×8 board with a single character and verifies that all 64 cells have the
   * expected {@link TileType} and {@link TileColour}.
   *
   * @param ch            the board character to test
   * @param expectedType  the {@link TileType} it should produce
   * @param expectedColour the {@link TileColour} it should produce
   */
  @ParameterizedTest(name = "''{0}'' -> {1} / {2}")
  @MethodSource("allBoardCharacters")
  void allCellsMatchExpected(char ch, TileType expectedType, TileColour expectedColour) {
    String row = String.valueOf(ch).repeat(CascadeBoard.WIDTH);
    CascadeBoard board = CascadeBoardBuilder.parse(row, row, row, row, row, row, row, row);

    for (int x = 0; x < CascadeBoard.WIDTH; x++) {
      for (int y = 0; y < CascadeBoard.HEIGHT; y++) {
        assertEquals(expectedType, board.getCell(x, y).type(),
            "type mismatch at (" + x + "," + y + ")");
        assertEquals(expectedColour, board.getCell(x, y).colour(),
            "colour mismatch at (" + x + "," + y + ")");
      }
    }
  }
}