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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TwistyPuzzle {

  public static final int HEIGHT =
      HD_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN - TITLE_HEIGHT - WIDGET_SPACING; // 1173
  public static final int WIDTH = HEIGHT * 4 / 3;// 800
  private final Map<Character, Color> colorMap = Arrays.stream(ColourEnum.values()).collect(
      Collectors.toMap(colourEnum -> colourEnum.getCharacter(),
          colourEnum -> colourEnum.getAwtColour()));
  private final Map<Character, Set<Integer>> faces = new HashMap<>();
  @Getter
  private final List<Cell> cells = new ArrayList<>();
  @Getter
  private final List<Move> moves = new ArrayList<>();
  @Getter
  @Setter
  private String baseState;
  @Getter
  @Setter
  private String puzzleName;
  @Getter
  private String currentState;
  @Getter
  private Map<String, CompiledMove> compiledMoves = new HashMap<>();

  /**
   * Compile the move based on the latest information
   *
   * @return returns an (optional) list of warnings
   */
  public Optional<String> compileMoves() {
    List<String> warnings = new ArrayList<>();
    // clear old compiled moves
    compiledMoves = new HashMap<>();
    for (Move move : moves) {
      CompiledMove cmove = new CompiledMove(cells.size());
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
      // check for duplicate name
      if (compiledMoves.containsKey(move.getName())) {
        warnings.add("Duplicate move name '" + move.getName() + "'");
      }
      compiledMoves.put(move.getName(), cmove);
    }
    if (warnings.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(String.join("\n", warnings));
    }
  }

  /**
   * populate the state and base state from the current cell values
   */
  public void takeSnapshot() {
    StringBuilder sb = new StringBuilder();
    for (Cell cell : cells) {
      sb.append(cell.getColour().getCharacter());
    }
    baseState = sb.toString();
    currentState = sb.toString();
  }

/*    public TwistyPuzzle(String tpResourceName, String name) {
        log.info("Creating TP Puzzle based on {}", tpResourceName);
        this.name = name;

        //  puzzle = (Puzzle) xstream.fromXML(TPPuzzle.class.getResourceAsStream(tpResourceName));
        log.info(
                "Loaded puzzle with {} cells and {} moves {}",
                getCells().size(),
                getMoves().size(),
                getCompiledMoves().keySet());
        // compile moves
        // work out initial state
        StringBuilder stringBuilder = new StringBuilder();
        for (Cell c : getCells()) {
            stringBuilder.append(c.getColour().getCharacter());
        }
        baseState = stringBuilder.toString();
        currentState = baseState;
        // create the default colour map
        colorMap.clear();
        for (ColourEnum colour : ColourEnum.values()) {
            colorMap.put(colour.getCharacter(), colour.getAwtColour());
        }
        // extract faces
        faces.clear();
        for (int i = 0; i < getCells().size(); i++) {
            Cell cell = getCells().get(i);
            char c = cell.getColour().getCharacter();
            if (faces.containsKey(c)) {
                faces.get(c).add(i);
            } else {
                Set<Integer> face = new HashSet<>();
                face.add(i);
                faces.put(c, face);
            }
        }
    }
*/


  public void resetPuzzle() {
    currentState = baseState;
  }


  public void resetPuzzle(String state) {
    currentState = state;
  }


  public Map<Character, Color> getColourMap() {
    return colorMap;
  }


  public String getState() {
    return currentState;
  }


  public List<String> getMoveList() {
    return List.copyOf(getCompiledMoves().keySet());
  }


  public int applyMove(String name) throws NotExistentMoveException {
    CompiledMove move = getCompiledMoves().get(name);
    if (move == null) {
      throw new NotExistentMoveException(name + " doesn't exist");
    }
    currentState = move.applyMove(currentState);
    return move.getCost();
  }


  public boolean isSolved() {
    for (Set<Integer> face : faces.values()) {
      // check all these cells have the same colour (dont care which).
      char c = ' ';
      for (Integer i : face) {
        if (' ' == c) {
          c = currentState.charAt(i);
        } else if (c != currentState.charAt(i)) {
          return false;
        }
      }
    }
    return true;
  }


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


  public BufferedImage getMoveImage(String move) {
    return getCompiledMoves().get(move).getImage();
  }


  public BufferedImage createMoveSpriteSheet() {
    // work out the image size
    int rows = getMoves().size() / 6;
    if (getMoves().size() % 6 > 0) {
      rows++;
    }
    BufferedImage image = new BufferedImage(Move.MOVE_ICON_WIDTH * 6, Move.MOVE_ICON_HEIGHT * rows,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    for (int i = 0; i < getMoves().size(); i++) {
      int x = i % 6;
      int y = i / 6;
      g.drawImage(getMoves().get(i).getImageIcon(), x * Move.MOVE_ICON_WIDTH,
          y * Move.MOVE_ICON_HEIGHT, null);
    }
    return image;
  }

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
