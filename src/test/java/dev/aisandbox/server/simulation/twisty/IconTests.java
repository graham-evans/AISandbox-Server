package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.engine.widget.GraphicsUtils;
import dev.aisandbox.server.simulation.twisty.model.CompiledMove;
import dev.aisandbox.server.simulation.twisty.model.Move;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class IconTests {

  private final static File outputDirectory = new File("build/test/twisty/icons");

  @BeforeAll
  public static void setupDir() {
    outputDirectory.mkdirs();
  }

  @ParameterizedTest
  @EnumSource(PuzzleType.class)
  public void testRunHighLowCards(PuzzleType ptype) throws IOException {
    // get all moves
    Map<String, CompiledMove> moves = ptype.getTwistyPuzzle().getCompiledMoves();
    List<String> movesNames = moves.keySet().stream().sorted().toList();
    BufferedImage image = GraphicsUtils.createBlankImage((Move.MOVE_ICON_WIDTH + 2) * moves.size(),
        Move.MOVE_ICON_HEIGHT + 2, Color.DARK_GRAY);
    Graphics2D g = image.createGraphics();
    int cursor = 1;
    for (String movesName : movesNames) {
      g.drawImage(moves.get(movesName).getImage(), cursor, 1, null);
      cursor += Move.MOVE_ICON_WIDTH;
      cursor++;
    }
    ImageIO.write(image, "png", new File(outputDirectory,ptype.name().toLowerCase() + ".png"));
  }

}
