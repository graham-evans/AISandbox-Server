package dev.aisandbox.server.simulation.twisty.model;

import static dev.aisandbox.server.engine.output.OutputConstants.BOTTOM_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.HD_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TITLE_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.TOP_MARGIN;
import static dev.aisandbox.server.engine.output.OutputConstants.WIDGET_SPACING;

import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.simulation.twisty.NotExistentMoveException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a twisty puzzle simulation such as a Rubik's cube or similar mechanical puzzle. The
 * puzzle consists of cells arranged in a specific structure with defined moves that can transform
 * the puzzle state. The class provides functionality for puzzle initialization, manipulation, state
 * tracking, and visual rendering.
 */
@Slf4j
@NoArgsConstructor
public class TwistyPuzzle {

  /**
   * The height of the puzzle rendering area in pixels. Calculated based on display constants from
   * OutputConstants.
   */
  public static final int HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173

  /**
   * The width of the puzzle rendering area in pixels. Set to maintain a 4:3 aspect ratio with the
   * height.
   */
  public static final int WIDTH = HEIGHT * 4 / 3;// 800

  /**
   * Maps color characters to their corresponding AWT Color objects. Generated from the ColourEnum
   * values.
   */
  private final Map<Character, Color> colorMap = Arrays.stream(ColourEnum.values()).collect(
      Collectors.toMap(colourEnum -> colourEnum.getCharacter(),
          colourEnum -> colourEnum.getAwtColour()));
  /**
   * The list of cells that make up the puzzle's structure.
   */
  @Getter
  private final List<Cell> cells = new ArrayList<>();

  /**
   * The number of cells on each side. As cells are added in order we can test that each set of
   * <i>faceSize</i> cells are the same colour.
   */
  @Getter
  List<Integer> faceSizes = new ArrayList<>();

  /**
   * The base (solved) state of the puzzle represented as a string of color characters. Each
   * character corresponds to the color of a cell in the solved position.
   */
  @Getter
  @Setter
  private String baseState;

  /**
   * The name of this puzzle type (e.g., "Rubik's Cube", "Pyraminx", etc.).
   */
  @Getter
  @Setter
  private String puzzleName;

  /**
   * The current state of the puzzle represented as a string of color characters. Each character
   * corresponds to the current color of a cell.
   */
  @Getter
  private String currentState;

  /**
   * Maps move names to their compiled representations for efficient application.
   */
  @Getter
  private Map<String, CompiledMove> compiledMoves = new HashMap<>();

  /**
   * Compiles and adds a move to the puzzle's available moves. Throws an exception if a move with
   * the same name already exists.
   *
   * @param move The move to compile and add
   * @throws IllegalArgumentException if a move with the same name already exists
   */
  public void addMove(Move move) {
    if (compiledMoves.containsKey(move.getName())) {
      throw new IllegalArgumentException("Move already exists: " + move.getName());
    }
    compiledMoves.put(move.getName(), move.compileMove(this));
  }

  /**
   * Takes a snapshot of the current cell colors and stores it as both the base state and current
   * state of the puzzle. This method should be called after initializing the puzzle's structure and
   * cell colors.
   */
  public void takeSnapshot() {
    StringBuilder sb = new StringBuilder();
    for (Cell cell : cells) {
      sb.append(cell.getColour().getCharacter());
    }
    baseState = sb.toString();
    currentState = sb.toString();
  }

  /**
   * Resets the puzzle to its base (solved) state.
   */
  public void resetPuzzle() {
    currentState = baseState;
  }

  /**
   * Resets the puzzle to a specific state.
   *
   * @param state The state to reset the puzzle to
   */
  public void resetPuzzle(String state) {
    currentState = state;
  }

  /**
   * Gets the mapping between color characters and their corresponding AWT Color objects.
   *
   * @return The color mapping
   */
  public Map<Character, Color> getColourMap() {
    return colorMap;
  }

  /**
   * Gets the current state of the puzzle.
   *
   * @return The current state as a string of color characters
   */
  public String getState() {
    return currentState;
  }

  /**
   * Gets a list of all available move names for this puzzle.
   *
   * @return An unmodifiable list of move names
   */
  public List<String> getMoveList() {
    return List.copyOf(getCompiledMoves().keySet());
  }

  /**
   * Applies a move to the puzzle by name, transforming the current state.
   *
   * @param name The name of the move to apply
   * @return The cost of the applied move
   * @throws NotExistentMoveException if the specified move doesn't exist
   */
  public MoveResult applyMove(String name) throws NotExistentMoveException {
    CompiledMove move = getCompiledMoves().get(name);
    if (move == null) {
      throw new NotExistentMoveException(name + " doesn't exist");
    }
    currentState = move.applyMove(currentState);
    return new MoveResult(move.getCost(), move.getImage());
  }

  /**
   * Checks if the puzzle is in a solved state. A puzzle is considered solved when all cells on each
   * face have the same color.
   *
   * @return true if the puzzle is solved, false otherwise
   */
  public boolean isSolved() {
    int cursor=0;
    for (int faceSize : faceSizes) {
      char c = ' ';
      for (int count=0;count<faceSize;count++) {
        if (' ' == c) {
          c = currentState.charAt(cursor);
        } else if (c != currentState.charAt(cursor)) {
          return false;
        }
        cursor++;
      }
    }
    log.info("Puzzle solved {}", currentState);
    return true;
  }

  /**
   * Renders the puzzle to a graphics context at the specified position.
   *
   * @param g       The graphics context to draw on
   * @param originX The x-coordinate of the top-left corner where drawing should start
   * @param originY The y-coordinate of the top-left corner where drawing should start
   * @param theme   The visual theme to use for rendering
   */
  public void drawPuzzle(Graphics2D g, int originX, int originY, Theme theme) {
    g.setColor(theme.getBaize());
    g.fillRect(originX, originY, WIDTH, HEIGHT);
    for (int i = 0; i < getCells().size(); i++) {
      Cell cell = getCells().get(i);
      char state = currentState.charAt(i);
      Polygon polygon = cell.getPolygon();
      polygon.translate(originX, originY);
      g.setColor(colorMap.get(state));
      g.fillPolygon(polygon);
      g.setColor(Color.LIGHT_GRAY);
      g.drawPolygon(polygon);
    }
  }

  /**
   * Centers the puzzle within its rendering area by adjusting cell positions. This ensures the
   * puzzle is visually centered when displayed.
   */
  public void centerPuzzle() {
    log.info("Centering puzzle");
    Rectangle2D bounds = cells.getFirst().getPolygon().getBounds2D();
    for (Cell cell : cells) {
      bounds = bounds.createUnion(cell.getPolygon().getBounds2D());
    }
    log.info("Initial bounds {}", bounds);
    int dx = (int) (-bounds.getMinX() + (WIDTH - bounds.getWidth()) / 2);
    int dy = (int) (-bounds.getMinY() + (HEIGHT - bounds.getHeight()) / 2);
    log.info("Moving cells {},{}", dx, dy);
    for (Cell cell : cells) {
      cell.setLocationX(dx + cell.getLocationX());
      cell.setLocationY(dy + cell.getLocationY());
    }
  }
}
