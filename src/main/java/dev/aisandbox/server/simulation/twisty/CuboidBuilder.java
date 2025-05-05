package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.simulation.twisty.model.Cell;
import dev.aisandbox.server.simulation.twisty.model.ColourEnum;
import dev.aisandbox.server.simulation.twisty.model.MoveLoop;
import dev.aisandbox.server.simulation.twisty.model.Move;
import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import dev.aisandbox.server.simulation.twisty.model.shapes.ShapeEnum;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CuboidBuilder {

  /***
   * The gap between sides as they are drawn
   */
  private static final int gap = 4;

  public static TwistyPuzzle buildCuboid(final int width, final int height, final int depth)
      throws IOException {
    TwistyPuzzle puzzle = new TwistyPuzzle();
    // work out the name of the puzzle
    puzzle.setPuzzleName(
        ((width == height) && (height == depth) ? "Cube " : "Cuboid ") + width + "x" + height + "x"
            + depth);
    // work out the scale of the cuboid
    int vscale = (TwistyPuzzle.HEIGHT - gap * 2) / ((height + depth * 2) * 2);
    int hscale = (TwistyPuzzle.WIDTH - gap * 3) / ((width * 2 + depth * 2) * 2);
    final int scale = Math.min(vscale, hscale);

    // generate sides
    log.info("Calculating sides of cuboid {}x{}x{} with scale {}", width, height, depth, scale);
    // create white (top) grid
    final List<Cell> top = new ArrayList<>(createGrid(0, 0, width, depth, ColourEnum.WHITE, scale));
    // create orange (left) grid
    final List<Cell> left = new ArrayList<>(
        createGrid(-depth * scale * 2 - gap, depth * scale * 2 + gap, depth, height,
            ColourEnum.ORANGE, scale));
    // create green (front) grid
    final List<Cell> front = new ArrayList<>(
        createGrid(0, depth * scale * 2 + gap, width, height, ColourEnum.GREEN, scale));
    // create red (right) grid
    final List<Cell> right = new ArrayList<>(
        createGrid(width * scale * 2 + gap, depth * scale * 2 + gap, depth, height, ColourEnum.RED,
            scale));
    // create blue (back) grid
    final List<Cell> back = new ArrayList<>(
        createGrid((width + depth) * scale * 2 + gap * 2, depth * scale * 2 + gap, width, height,
            ColourEnum.BLUE, scale));
    // create yellow (bottom) grid
    final List<Cell> bottom = new ArrayList<>(
        createGrid(0, (depth + height) * scale * 2 + gap * 2, width, depth, ColourEnum.YELLOW,
            scale));
    // add all cells to the puzzle
    puzzle.getCells().addAll(left);
    puzzle.getCells().addAll(right);
    puzzle.getCells().addAll(top);
    puzzle.getCells().addAll(bottom);
    puzzle.getCells().addAll(front);
    puzzle.getCells().addAll(back);
    // create moves
    if (width == height) { // we can rotate the faces 90' left or right
      // we can have F,F',B,B',z,z' moves
      for (int deep = 1; deep < depth; deep++) {
        // create F moves
        log.info("Generating F at depth {}", deep);
        Move fMove = new Move();
        fMove.setName(getMoveName(deep, 'F', 1));
        fMove.setImageIcon(CuboidMoveIcon.builer(width,height, fMove.getName()).fillFrontFace().setRotation('F', false).getImage());
        fMove.getLoops().addAll(faceTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          fMove.getLoops()
              .addAll(frontSideTurn(layer, width, height, depth, left, right, top, bottom));
        }
        puzzle.getMoves().add(fMove);
        // create F' move
        log.info("Generating F' at depth {}", deep );
        Move fPrimeMove = new Move();
        fPrimeMove.setName(getMoveName(deep, 'F', -1));
        fPrimeMove.setImageIcon(CuboidMoveIcon.builer(width,height, fPrimeMove.getName()).fillFrontFace().setRotation('F', true).getImage());
        fPrimeMove.getLoops().addAll(faceReverseTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          fPrimeMove.getLoops()
              .addAll(frontSideReverseTurn(layer, width, height, depth, left, right, top, bottom));
        }
        puzzle.getMoves().add(fPrimeMove);
        // B moves
        log.info("Generating B at depth {}", deep);
        Move bMove = new Move();
        bMove.setName(getMoveName(deep, 'B', 1));
        bMove.setImageIcon(CuboidMoveIcon.builer(width,height, bMove.getName()).setRotation('B', false).getImage());
        bMove.getLoops().addAll(faceTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          bMove.getLoops().addAll(
              frontSideReverseTurn(depth - layer + 1, width, height, depth, left, right, top,
                  bottom));
        }
        puzzle.getMoves().add(bMove);
        // B' moves
        log.info("Generating B' at depth {}", deep);
        Move bPrimeMove = new Move();
        bPrimeMove.setName(getMoveName(deep, 'B', -1));
        bMove.setImageIcon(CuboidMoveIcon.builer(width,height, bMove.getName()).setRotation('B', true).getImage());
        bPrimeMove.getLoops().addAll(faceReverseTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          bPrimeMove.getLoops().addAll(
              frontSideTurn(depth - layer + 1, width, height, depth, left, right, top, bottom));
        }
        puzzle.getMoves().add(bPrimeMove);
      }
      // z move
      log.info("Generating Z");
      Move zMove = new Move();
      zMove.setName(getMoveName(0, 'F', 1));
      zMove.setImageIcon(CuboidMoveIcon.builer(width,height, zMove.getName()).fillFrontFace().setRotation('F', false).getImage());
      zMove.getLoops().addAll(faceTurn(front, width, height));
      zMove.getLoops().addAll(faceReverseTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        zMove.getLoops()
            .addAll(frontSideTurn(layer, width, height, depth, left, right, top, bottom));
      }
      zMove.setCost(0);
      puzzle.getMoves().add(zMove);
      // z' move
      log.info("Generating Z'");
      Move zPrimeMove = new Move();
      zPrimeMove.setName(getMoveName(0, 'F', -1));
      zPrimeMove.setImageIcon(CuboidMoveIcon.builer(width,height, zPrimeMove.getName()).fillFrontFace().setRotation('F', true).getImage());
      zPrimeMove.getLoops().addAll(faceReverseTurn(front, width, height));
      zPrimeMove.getLoops().addAll(faceTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        zPrimeMove.getLoops()
            .addAll(frontSideReverseTurn(layer, width, height, depth, left, right, top, bottom));
      }
      zPrimeMove.setCost(0);
      puzzle.getMoves().add(zPrimeMove);
    }
    if (width == depth) {
      // we can have U,U',D,D',y,y'
      for (int deep = 1; deep < height; deep++) {
        // U Move
        log.info("Generating U to depth {}", deep);
        Move uMove = new Move();
        uMove.setName(getMoveName(deep, 'U', 1));
        uMove.setImageIcon(CuboidMoveIcon.builer(width,height, uMove.getName()).fillFromTop(deep).setRotation('U', false).getImage());
        uMove.getLoops().addAll(faceTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          uMove.getLoops()
              .addAll(topSideTurn(layer, width, height, depth, left, right, front, back));
         }
         puzzle.getMoves().add(uMove);
        // U' move
        log.info("Generating U' to depth {}", deep);
        Move uPrimeMove = new Move();
        uPrimeMove.setName(getMoveName(deep, 'U', -1));
        uPrimeMove.setImageIcon(CuboidMoveIcon.builer(width,height, uPrimeMove.getName()).fillFromTop(deep).setRotation('U', true).getImage());
        uPrimeMove.getLoops().addAll(faceReverseTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          uPrimeMove.getLoops()
              .addAll(topSideReverseTurn(layer, width, height, depth, left, right, front, back));
        }
        puzzle.getMoves().add(uPrimeMove);
        // D move
        Move dMove = new Move();
        dMove.setName(getMoveName(deep, 'D', 1));
   //     icon = new CuboidMoveIcon(width, height);
   //     icon.setRotation('D', false);
        dMove.getLoops().addAll(faceTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          dMove.getLoops().addAll(
              topSideReverseTurn(height - layer + 1, width, height, depth, left, right, front,
                  back));
          for (int x = 0; x < width; x++) {
   //         icon.fillFrontFace(x, height - layer);
          }
        }
  //      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'D', 1)));
        puzzle.getMoves().add(dMove);
        // D' move
        Move dPrimeMove = new Move();
        dPrimeMove.setName(getMoveName(deep, 'D', -1));
  //      icon = new CuboidMoveIcon(width, height);
  //      icon.setRotation('D', true);
        dPrimeMove.getLoops().addAll(faceReverseTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          dPrimeMove.getLoops().addAll(
              topSideTurn(height - layer + 1, width, height, depth, left, right, front, back));
          for (int x = 0; x < width; x++) {
  //          icon.fillFrontFace(x, height - layer);
          }
        }
  //      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'D', -1)));
        puzzle.getMoves().add(dPrimeMove);
      }
      // y move
      Move yMove = new Move();
      yMove.setName(getMoveName(0, 'U', 1));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('U', false);
//      icon.fillFrontFace();
 //     move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'U', 1)));
      yMove.getLoops().addAll(faceTurn(top, width, depth));
      yMove.getLoops().addAll(faceReverseTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        yMove.getLoops().addAll(topSideTurn(layer, width, height, depth, left, right, front, back));
      }
      yMove.setCost(0);
      puzzle.getMoves().add(yMove);
      // y' move
      Move yPrimeMove = new Move();
      yPrimeMove.setName(getMoveName(0, 'U', -1));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('U', true);
//      icon.fillFrontFace();
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'U', -1)));
      yPrimeMove.getLoops().addAll(faceReverseTurn(top, width, depth));
      yPrimeMove.getLoops().addAll(faceTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        yPrimeMove.getLoops()
            .addAll(topSideReverseTurn(layer, width, height, depth, left, right, front, back));
      }
      yPrimeMove.setCost(0);
      puzzle.getMoves().add(yPrimeMove);
    }
    if (depth == height) {
      // we can have R,R',L,L',z,z'
      for (int deep = 1; deep < width; deep++) {
        // R
        Move rMove = new Move();
        rMove.setName(getMoveName(deep, 'R', 1));
//        icon = new CuboidMoveIcon(width, height);
//        icon.setRotation('R', false);
        rMove.getLoops().addAll(faceTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          rMove.getLoops()
              .addAll(rightSideTurn(layer, width, height, depth, front, back, top, bottom));
          for (int y = 0; y < height; y++) {
//            icon.fillFrontFace(width - layer, y);
          }
        }
//        move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'R', 1)));
        puzzle.getMoves().add(rMove);
        // R'
        Move rPrimeMove = new Move();
        rPrimeMove.setName(getMoveName(deep, 'R', -1));
//        icon = new CuboidMoveIcon(width, height);
//        icon.setRotation('R', true);
        rPrimeMove.getLoops().addAll(faceReverseTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          rPrimeMove.getLoops()
              .addAll(rightSideReverseTurn(layer, width, height, depth, front, back, top, bottom));
          for (int y = 0; y < height; y++) {
//            icon.fillFrontFace(width - layer, y);
          }
        }
//        move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'R', -1)));
        puzzle.getMoves().add(rPrimeMove);
        // L
        Move lMove = new Move();
        lMove.setName(getMoveName(deep, 'L', 1));
//        icon = new CuboidMoveIcon(width, height);
        //icon.setRotation('L', false);
        lMove.getLoops().addAll(faceTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          lMove.getLoops().addAll(
              rightSideReverseTurn(width - layer + 1, width, height, depth, front, back, top,
                  bottom));
          for (int y = 0; y < height; y++) {
//            icon.fillFrontFace(layer - 1, y);
          }
        }
//        move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'L', 1)));
        puzzle.getMoves().add(lMove);
        // L'
        Move lPrimeMove = new Move();
        lPrimeMove.setName(getMoveName(deep, 'L', -1));
//        icon = new CuboidMoveIcon(width, height);
//        icon.setRotation('L', true);
        lPrimeMove.getLoops().addAll(faceReverseTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          lPrimeMove.getLoops().addAll(
              rightSideTurn(width - layer + 1, width, height, depth, front, back, top, bottom));
          for (int y = 0; y < height; y++) {
//            icon.fillFrontFace(layer - 1, y);
          }
        }
//        move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'L', -1)));
        puzzle.getMoves().add(lPrimeMove);
      }
      // z
      Move zMove = new Move();
      zMove.setName(getMoveName(0, 'R', 1));
//      icon = new CuboidMoveIcon(width, height);
//      icon.fillFrontFace();
//      icon.setRotation('R', false);
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'R', 1)));
      zMove.getLoops().addAll(faceTurn(right, depth, height));
      zMove.getLoops().addAll(faceReverseTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        zMove.getLoops()
            .addAll(rightSideTurn(layer, width, height, depth, front, back, top, bottom));
      }
      zMove.setCost(0);
      puzzle.getMoves().add(zMove);
      // z'
      Move zPrimeMove = new Move();
      zPrimeMove.setName(getMoveName(0, 'R', -1));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('R', true);
//      icon.fillFrontFace();
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'R', -1)));
      zPrimeMove.getLoops().addAll(faceReverseTurn(right, depth, height));
      zPrimeMove.getLoops().addAll(faceTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        zPrimeMove.getLoops()
            .addAll(rightSideReverseTurn(layer, width, height, depth, front, back, top, bottom));
      }
      zPrimeMove.setCost(0);
      puzzle.getMoves().add(zPrimeMove);
    }
    // we can always have double turns
    for (int deep = 1; deep < depth; deep++) {
      // F2 moves
      Move f2Move = new Move();
      f2Move.setName(getMoveName(deep, 'F', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('F', false);
//      icon.fillFrontFace();
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'F', 2)));
      f2Move.getLoops().addAll(faceDoubleTurn(front, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        f2Move.getLoops()
            .addAll(frontSideDoubleTurn(layer, width, height, depth, left, right, top, bottom));
      }
      puzzle.getMoves().add(f2Move);
      // B2
      Move b2Move = new Move();
      b2Move.setName(getMoveName(deep, 'B', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('B', false);
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'B', 2)));
      b2Move.getLoops().addAll(faceDoubleTurn(back, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        b2Move.getLoops().addAll(
            frontSideDoubleTurn(depth - layer + 1, width, height, depth, left, right, top, bottom));
      }
      puzzle.getMoves().add(b2Move);
    }
    // z2
    Move z2Move = new Move();
    z2Move.setName(getMoveName(0, 'F', 2));
//    icon = new CuboidMoveIcon(width, height);
//    icon.fillFrontFace();
//    icon.setRotation('F', false);
//    move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'F', 2)));
    z2Move.getLoops().addAll(faceDoubleTurn(front, width, height));
    z2Move.getLoops().addAll(faceDoubleTurn(back, width, height));
    for (int layer = 1; layer <= depth; layer++) {
      z2Move.getLoops()
          .addAll(frontSideDoubleTurn(layer, width, height, depth, left, right, top, bottom));
    }
    z2Move.setCost(0);
    puzzle.getMoves().add(z2Move);
    for (int deep = 1; deep < height; deep++) {
      // U2
      Move u2Move = new Move();
      u2Move.setName(getMoveName(deep, 'U', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('U', false);
      u2Move.getLoops().addAll(faceDoubleTurn(top, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        u2Move.getLoops()
            .addAll(topSideDoubleTurn(layer, width, height, depth, right, left, front, back));
        for (int x = 0; x < width; x++) {
  //        icon.fillFrontFace(x, layer - 1);
        }
      }
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'U', 2)));
      puzzle.getMoves().add(u2Move);
      //  D2
      Move d2Move = new Move();
      d2Move.setName(getMoveName(deep, 'D', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('D', false);
      d2Move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        d2Move.getLoops().addAll(
            topSideDoubleTurn(height - layer + 1, width, height, depth, right, left, front, back));
        for (int x = 0; x < width; x++) {
//          icon.fillFrontFace(x, height - layer);
        }
      }
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'D', 2)));
      puzzle.getMoves().add(d2Move);
    }
    //  y2
    Move y2Move = new Move();
    y2Move.setName(getMoveName(0, 'U', 2));
//    icon = new CuboidMoveIcon(width, height);
//    icon.setRotation('U', false);
//    icon.fillFrontFace();
//    move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'U', 2)));
    y2Move.getLoops().addAll(faceDoubleTurn(top, width, depth));
    y2Move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
    for (int layer = 1; layer <= height; layer++) {
      y2Move.getLoops()
          .addAll(topSideDoubleTurn(layer, width, height, depth, right, left, front, back));
    }
    y2Move.setCost(0);
    puzzle.getMoves().add(y2Move);
    for (int deep = 1; deep < width; deep++) {
      //  R2
      Move r2Move = new Move();
      r2Move.setName(getMoveName(deep, 'R', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('R', false);
      r2Move.getLoops().addAll(faceDoubleTurn(right, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        r2Move.getLoops()
            .addAll(rightSideDoubleTurn(layer, width, height, depth, front, back, top, bottom));
        for (int y = 0; y < height; y++) {
//          icon.fillFrontFace(width - layer, y);
        }
      }
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'R', 2)));
      puzzle.getMoves().add(r2Move);
      //  L2
      Move l2Move = new Move();
      l2Move.setName(getMoveName(deep, 'L', 2));
//      icon = new CuboidMoveIcon(width, height);
//      icon.setRotation('L', false);
      l2Move.getLoops().addAll(faceDoubleTurn(left, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        l2Move.getLoops().addAll(
            rightSideDoubleTurn(width - layer + 1, width, height, depth, front, back, top, bottom));
        for (int y = 0; y < height; y++) {
//          icon.fillFrontFace(layer - 1, y);
        }
      }
//      move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(deep, 'L', 2)));
      puzzle.getMoves().add(l2Move);
    }
    // x2
    Move x2Move = new Move();
    x2Move.setName(getMoveName(0, 'R', 2));
//    icon = new CuboidMoveIcon(width, height);
//    icon.setRotation('R', false);
//    icon.fillFrontFace();
//    move.setImageIcon(getMoveIcon(icon.getImage(), getMoveName(0, 'R', 2)));
    x2Move.getLoops().addAll(faceDoubleTurn(right, depth, height));
    x2Move.getLoops().addAll(faceDoubleTurn(left, depth, height));
    for (int layer = 1; layer <= width; layer++) {
      x2Move.getLoops()
          .addAll(rightSideDoubleTurn(layer, width, height, depth, front, back, top, bottom));
    }
    x2Move.setCost(0);
    puzzle.getMoves().add(x2Move);
    // compile moves
    Optional<String> errors = puzzle.compileMoves();
    errors.ifPresent(s -> log.warn("Error compiling moves: {}", s));
    // center puzzle
    puzzle.centerPuzzle();
    // set base state
    puzzle.takeSnapshot();

    log.info("Finished building puzzle with {} cells, {} moves, {} compiled moves",
        puzzle.getCells().size(), puzzle.getMoves().size(), puzzle.getCompiledMoves().size());
    return puzzle;

  }

  /**
   * Create the move name, if depth<1 then assume a cube rotation
   *
   * @param depth        the depth of the turn 0 for all layers
   * @param face         the face to turn
   * @param quarterTurns the number of quarter turns
   * @return
   */
  public static String getMoveName(int depth, char face, int quarterTurns) {
    StringBuilder result = new StringBuilder();
    // outer block moves
    if (depth > 2) {
      result.append(depth);
    }
    // face
    if (depth > 0) {
      result.append(face);
    } else {
      switch (face) {
        case 'R':
          result.append("x");
          break;
        case 'U':
          result.append("y");
          break;
        case 'F':
          result.append("z");
          break;
        default:
          result.append("?");
      }
    }
    // outer block move
    if (depth > 1) {
      result.append("w");
    }
    // rotations
    if (quarterTurns == 2) {
      result.append("2");
    } else if (quarterTurns == -1) {
      result.append("'");
    }
    return result.toString();
  }

  private List<Cell> createGrid(int x, int y, int w, int h, ColourEnum colour, int scale) {
    List<Cell> cells = new ArrayList<>();
    for (int dy = 0; dy < h; dy++) {
      for (int dx = 0; dx < w; dx++) {
        Cell c = new Cell();
        c.setShape(ShapeEnum.SQUARE);
        c.setColour(colour);
        c.setLocationX(x + dx * scale * 2);
        c.setLocationY(y + dy * scale * 2);
        c.setScale(scale);
        c.setRotation(0);
        cells.add(c);
      }
    }
    return cells;
  }

  private List<MoveLoop> frontSideTurn(int layer, int width, int height, int depth, List<Cell> left,
      List<Cell> right, List<Cell> top, List<Cell> bottom) {
    if (width != height) {
      throw new IllegalStateException();
    }
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      MoveLoop loop = new MoveLoop();
      loop.getCells().add(top.get(i + width * (depth - layer)));
      loop.getCells().add(right.get(layer - 1 + i * depth));
      loop.getCells().add(bottom.get(width - i - 1 + width * (layer - 1)));
      loop.getCells().add(left.get(depth - layer + (height - i - 1) * depth));
      result.add(loop);
    }
    return result;
  }

  private List<MoveLoop> topSideTurn(int layer, int width, int height, int depth, List<Cell> left,
      List<Cell> right, List<Cell> front, List<Cell> back) {
    if (width != depth) {
      throw new IllegalStateException();
    }
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      MoveLoop loop = new MoveLoop();
      loop.getCells().add(front.get(i + (layer - 1) * width));
      loop.getCells().add(left.get(i + (layer - 1) * width));
      loop.getCells().add(back.get(i + (layer - 1) * width));
      loop.getCells().add(right.get(i + (layer - 1) * depth));
      result.add(loop);
    }
    return result;
  }

  private List<MoveLoop> rightSideTurn(int layer, int width, int height, int depth, List<Cell> front,
      List<Cell> back, List<Cell> top, List<Cell> bottom) {
    if (height != depth) {
      throw new IllegalStateException();
    }
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      MoveLoop loop = new MoveLoop();
      loop.getCells().add(front.get(width - layer + i * width));
      loop.getCells().add(top.get(width - layer + i * width));
      loop.getCells().add(back.get(layer - 1 + (height - i - 1) * width));
      loop.getCells().add(bottom.get(width - layer + i * width));
      result.add(loop);
    }
    return result;
  }

  private List<MoveLoop> frontSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = frontSideTurn(layer, width, height, depth, left, right, top, bottom);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<MoveLoop> topSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> front, List<Cell> back) {
    List<MoveLoop> result = topSideTurn(layer, width, height, depth, left, right, front, back);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<MoveLoop> rightSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> front, List<Cell> back, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = rightSideTurn(layer, width, height, depth, front, back, top, bottom);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<MoveLoop> frontSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(top.get(i + (depth - layer) * width));
      l.getCells().add(bottom.get(width - i - 1 + (layer - 1) * width));
      result.add(l);
    }
    for (int i = 0; i < height; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(right.get(layer - 1 + i * depth));
      l.getCells().add(left.get(depth - layer + (height - i - 1) * depth));
      result.add(l);
    }
    return result;
  }

  private List<MoveLoop> topSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> right, List<Cell> left, List<Cell> front, List<Cell> back) {
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(front.get(i + (layer - 1) * width));
      l.getCells().add(back.get(i + (layer - 1) * width));
      result.add(l);
    }
    for (int i = 0; i < depth; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(right.get(i + (layer - 1) * depth));
      l.getCells().add(left.get(i + (layer - 1) * depth));
      result.add(l);
    }
    return result;
  }

  private List<MoveLoop> rightSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> front, List<Cell> back, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(front.get(i * width + width - layer));
      l.getCells().add(back.get((height - i - 1) * width + layer - 1));
      result.add(l);
    }
    for (int i = 0; i < depth; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(top.get(i * width + width - layer));
      l.getCells().add(bottom.get(i * width + width - layer));
      result.add(l);
    }
    return result;
  }

  private List<MoveLoop> faceTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = new ArrayList<>();
    for (int dx = 0; dx < divRoundUp(width, 2); dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        MoveLoop loop = new MoveLoop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get((width - dy - 1) + width * dx));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        loop.getCells().add(face.get(dy + width * (height - dx - 1)));
        result.add(loop);
      }
    }
    return result;
  }

  private List<MoveLoop> faceReverseTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = faceTurn(face, width, height);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<MoveLoop> faceDoubleTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = new ArrayList<>();
    for (int dx = 0; dx < width; dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        MoveLoop loop = new MoveLoop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        result.add(loop);
      }
    }
    // special case for odd numbered heights
    if (height % 2 == 1) {
      int dy = divRoundUp(height, 2) - 1;
      for (int dx = 0; dx < width / 2; dx++) {
        MoveLoop loop = new MoveLoop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get(width - dx - 1 + dy * width));
        result.add(loop);
      }
    }
    return result;
  }

  public static int divRoundUp(int num, int divisor) {
    return (num + divisor - 1) / divisor;
  }




}
