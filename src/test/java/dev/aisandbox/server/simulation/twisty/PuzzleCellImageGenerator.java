/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.simulation.twisty.model.Cell;
import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Generates and validates starting state images for Twisty puzzle types. */
@Slf4j
public class PuzzleCellImageGenerator {

  private static final File outputDirectory = new File("build/test/twisty-start");
  private static final int CIRCLE_SIZE = 50;

  /** Initializes the output directory for puzzle cell images. */
  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

  /**
   * Generates and saves a cell-labeled image of a puzzle's starting configuration.
   *
   * @param puzzleType the puzzle type to generate an image for
   * @throws IOException if image writing fails
   */
  @ParameterizedTest
  @EnumSource(PuzzleType.class)
  public void generatePuzzleCellImage(PuzzleType puzzleType) throws IOException {
    log.info("Generating image of {}", puzzleType.name());
    TwistyPuzzle puzzle = puzzleType.getTwistyPuzzle();
    BufferedImage image = new BufferedImage(TwistyPuzzle.WIDTH, TwistyPuzzle.HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    puzzle.drawPuzzle(graphics, 0, 0, Theme.LIGHT);
    for (int i = 0; i < puzzle.getCells().size(); i++) {
      Cell cell = puzzle.getCells().get(i);
      graphics.setColor(Color.BLACK);
      graphics.fillOval(cell.getLocationX() - CIRCLE_SIZE / 2,
          cell.getLocationY() - CIRCLE_SIZE / 2, CIRCLE_SIZE, CIRCLE_SIZE);
      graphics.setColor(Color.WHITE);
      GraphicsUtils.drawCenteredText(graphics, cell.getLocationX() - CIRCLE_SIZE / 2,
          cell.getLocationY() - OutputConstants.STATISTICS_FONT.getSize() / 2 - 4, CIRCLE_SIZE,
          OutputConstants.STATISTICS_FONT.getSize(), Integer.toString(i),
          OutputConstants.STATISTICS_FONT, Color.WHITE);
    }
    File outputFile = new File(outputDirectory, puzzleType.name() + ".png");
    ImageIO.write(image, "png", outputFile);
    assertTrue(outputFile.isFile());
  }

}
