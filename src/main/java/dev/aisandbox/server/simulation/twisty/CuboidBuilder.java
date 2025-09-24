/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.simulation.twisty.model.Cell;
import dev.aisandbox.server.simulation.twisty.model.ColourEnum;
import dev.aisandbox.server.simulation.twisty.model.Move;
import dev.aisandbox.server.simulation.twisty.model.MoveLoop;
import dev.aisandbox.server.simulation.twisty.model.TwistyPuzzle;
import dev.aisandbox.server.simulation.twisty.model.shapes.ShapeEnum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for building cuboid twisty puzzles like cubes, 2x3x3 cuboids, etc. Provides methods
 * to create the puzzle structure, define the faces, and set up all the possible moves for the
 * puzzle.
 */
@Slf4j
@UtilityClass
public class CuboidBuilder {

  /**
   * The visual gap between sides when rendering the puzzle. Used to separate faces for better
   * visibility.
   */
  private static final int gap = 4;

  /**
   * Builds a cuboid puzzle with the specified dimensions. Creates all cells, faces, and valid moves
   * for the puzzle. Handles special cases like standard cubes with equal dimensions vs. non-cubic
   * cuboids.
   *
   * @param width  The width (x-dimension) of the cuboid
   * @param height The height (y-dimension) of the cuboid
   * @param depth  The depth (z-dimension) of the cuboid
   * @return A fully initialized TwistyPuzzle representing the cuboid
   * @throws IOException If there's an error creating or processing the puzzle
   */
  public static TwistyPuzzle buildCuboid(final int width, final int height, final int depth)
      throws IOException {
    TwistyPuzzle puzzle = new TwistyPuzzle();

    // Set puzzle name based on whether it's a cube (all dimensions equal) or a cuboid
    puzzle.setPuzzleName(
        ((width == height) && (height == depth) ? "Cube " : "Cuboid ") + width + "x" + height + "x"
            + depth);

    // Calculate appropriate scale to fit the puzzle within the display area
    int vscale = (TwistyPuzzle.HEIGHT - gap * 2) / ((height + depth * 2) * 2);
    int hscale = (TwistyPuzzle.WIDTH - gap * 3) / ((width * 2 + depth * 2) * 2);
    final int scale = Math.min(vscale, hscale);

    // Generate the six faces of the cuboid with appropriate positions and colors
    log.info("Calculating sides of cuboid {}x{}x{} with scale {}", width, height, depth, scale);

    // Create white (top) grid
    final List<Cell> top = new ArrayList<>(createGrid(0, 0, width, depth, ColourEnum.WHITE, scale));

    // Create orange (left) grid
    final List<Cell> left = new ArrayList<>(
        createGrid(-depth * scale * 2 - gap, depth * scale * 2 + gap, depth, height,
            ColourEnum.ORANGE, scale));

    // Create green (front) grid
    final List<Cell> front = new ArrayList<>(
        createGrid(0, depth * scale * 2 + gap, width, height, ColourEnum.GREEN, scale));

    // Create red (right) grid
    final List<Cell> right = new ArrayList<>(
        createGrid(width * scale * 2 + gap, depth * scale * 2 + gap, depth, height, ColourEnum.RED,
            scale));

    // Create blue (back) grid
    final List<Cell> back = new ArrayList<>(
        createGrid((width + depth) * scale * 2 + gap * 2, depth * scale * 2 + gap, width, height,
            ColourEnum.BLUE, scale));

    // Create yellow (bottom) grid
    final List<Cell> bottom = new ArrayList<>(
        createGrid(0, (depth + height) * scale * 2 + gap * 2, width, depth, ColourEnum.YELLOW,
            scale));

    // Add all cells to the puzzle (order is important)
    puzzle.getCells().addAll(top);
    puzzle.getFaceSizes().add(top.size());
    puzzle.getCells().addAll(left);
    puzzle.getFaceSizes().add(left.size());
    puzzle.getCells().addAll(front);
    puzzle.getFaceSizes().add(front.size());
    puzzle.getCells().addAll(right);
    puzzle.getFaceSizes().add(right.size());
    puzzle.getCells().addAll(back);
    puzzle.getFaceSizes().add(back.size());
    puzzle.getCells().addAll(bottom);
    puzzle.getFaceSizes().add(bottom.size());
    // Create moves based on puzzle dimensions

    // Front face moves (if width equals height, we can do 90° rotations)
    if (width == height) {
      // Create F, F', B, B', z, z' moves
      for (int deep = 1; deep < depth; deep++) {
        // Create F moves (clockwise front face rotation)
        log.info("Generating F at depth {}", deep);
        Move frontMove = new Move();
        frontMove.setName(getMoveName(deep, 'F', 1));
        frontMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, frontMove.getName()).fillFrontFace()
                .setRotation('F', false).getImage());
        frontMove.getLoops().addAll(faceTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          frontMove.getLoops()
              .addAll(frontSideTurn(layer, width, height, depth, left, right, top, bottom));
        }
        frontMove.setCost(1);
        puzzle.addMove(frontMove);

        // Create F' moves (counterclockwise front face rotation)
        log.info("Generating F' at depth {}", deep);
        Move frontPrimeMove = new Move();
        frontPrimeMove.setName(getMoveName(deep, 'F', -1));
        frontPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, frontPrimeMove.getName()).fillFrontFace()
                .setRotation('F', true).getImage());
        frontPrimeMove.getLoops().addAll(faceReverseTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          frontPrimeMove.getLoops()
              .addAll(frontSideReverseTurn(layer, width, height, depth, left, right, top, bottom));
        }
        frontPrimeMove.setCost(1);
        puzzle.addMove(frontPrimeMove);

        // Create B moves (clockwise back face rotation)
        log.info("Generating B at depth {}", deep);
        Move backMove = new Move();
        backMove.setName(getMoveName(deep, 'B', 1));
        backMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, backMove.getName()).setRotation('B', false)
                .getImage());
        backMove.getLoops().addAll(faceTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          backMove.getLoops().addAll(
              frontSideReverseTurn(depth - layer + 1, width, height, depth, left, right, top,
                  bottom));
        }
        backMove.setCost(1);
        puzzle.addMove(backMove);

        // Create B' moves (counterclockwise back face rotation)
        log.info("Generating B' at depth {}", deep);
        Move backPrimeMove = new Move();
        backPrimeMove.setName(getMoveName(deep, 'B', -1));
        backPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, backPrimeMove.getName())
                .setRotation('B', true).getImage());
        backPrimeMove.getLoops().addAll(faceReverseTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          backPrimeMove.getLoops().addAll(
              frontSideTurn(depth - layer + 1, width, height, depth, left, right, top, bottom));
        }
        backPrimeMove.setCost(1);
        puzzle.addMove(backPrimeMove);
      }

      // Create z move (whole puzzle rotation around front-back axis, no cost)
      log.info("Generating Z");
      Move frontRotationMove = new Move();
      frontRotationMove.setName(getMoveName(0, 'F', 1));
      frontRotationMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, frontRotationMove.getName()).fillFrontFace()
              .setRotation('F', false).getImage());
      frontRotationMove.getLoops().addAll(faceTurn(front, width, height));
      frontRotationMove.getLoops().addAll(faceReverseTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        frontRotationMove.getLoops()
            .addAll(frontSideTurn(layer, width, height, depth, left, right, top, bottom));
      }
      frontRotationMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(frontRotationMove);

      // Create z' move (inverse whole puzzle rotation, no cost)
      log.info("Generating Z'");
      Move frontRotationPrimeMove = new Move();
      frontRotationPrimeMove.setName(getMoveName(0, 'F', -1));
      frontRotationPrimeMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, frontRotationPrimeMove.getName())
              .fillFrontFace().setRotation('F', true).getImage());
      frontRotationPrimeMove.getLoops().addAll(faceReverseTurn(front, width, height));
      frontRotationPrimeMove.getLoops().addAll(faceTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        frontRotationPrimeMove.getLoops()
            .addAll(frontSideReverseTurn(layer, width, height, depth, left, right, top, bottom));
      }
      frontRotationPrimeMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(frontRotationPrimeMove);
    }

    // Top face moves (if width equals depth, we can do 90° rotations)
    if (width == depth) {
      // Create U, U', D, D', y, y' moves
      for (int deep = 1; deep < height; deep++) {
        // Create U move (clockwise top face rotation)
        log.info("Generating U to depth {}", deep);
        Move upMove = new Move();
        upMove.setName(getMoveName(deep, 'U', 1));
        upMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, upMove.getName()).fillFromTop(deep)
                .setRotation('U', false).getImage());
        upMove.getLoops().addAll(faceTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          upMove.getLoops()
              .addAll(topSideTurn(layer, width, height, depth, left, right, front, back));
        }
        upMove.setCost(1);
        puzzle.addMove(upMove);

        // Create U' move (counterclockwise top face rotation)
        log.info("Generating U' to depth {}", deep);
        Move upPrimeMove = new Move();
        upPrimeMove.setName(getMoveName(deep, 'U', -1));
        upPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, upPrimeMove.getName()).fillFromTop(deep)
                .setRotation('U', true).getImage());
        upPrimeMove.getLoops().addAll(faceReverseTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          upPrimeMove.getLoops()
              .addAll(topSideReverseTurn(layer, width, height, depth, left, right, front, back));
        }
        upPrimeMove.setCost(0);
        puzzle.addMove(upPrimeMove);

        // Create D move (clockwise bottom face rotation)
        log.info("Generating D to depth {}", deep);
        Move downMove = new Move();
        downMove.setName(getMoveName(deep, 'D', 1));
        downMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, downMove.getName()).fillFromBottom(deep)
                .setRotation('D', false).getImage());
        downMove.getLoops().addAll(faceTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          downMove.getLoops().addAll(
              topSideReverseTurn(height - layer + 1, width, height, depth, left, right, front,
                  back));
        }
        downMove.setCost(1);
        puzzle.addMove(downMove);

        // Create D' move (counterclockwise bottom face rotation)
        log.info("Generating D' to depth {}", deep);
        Move downPrimeMove = new Move();
        downPrimeMove.setName(getMoveName(deep, 'D', -1));
        downPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, downPrimeMove.getName())
                .fillFromBottom(deep).setRotation('D', true).getImage());
        downPrimeMove.getLoops().addAll(faceReverseTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          downPrimeMove.getLoops().addAll(
              topSideTurn(height - layer + 1, width, height, depth, left, right, front, back));
        }
        downPrimeMove.setCost(1);
        puzzle.addMove(downPrimeMove);
      }

      // Create y move (whole puzzle rotation around top-bottom axis, no cost)
      Move topRotationMove = new Move();
      log.info("Generating y");
      topRotationMove.setName(getMoveName(0, 'U', 1));
      topRotationMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, topRotationMove.getName()).fillFrontFace()
              .setRotation('U', false).getImage());
      topRotationMove.getLoops().addAll(faceTurn(top, width, depth));
      topRotationMove.getLoops().addAll(faceReverseTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        topRotationMove.getLoops()
            .addAll(topSideTurn(layer, width, height, depth, left, right, front, back));
      }
      topRotationMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(topRotationMove);

      // Create y' move (inverse whole puzzle rotation, no cost)
      log.info("Generating y'");
      Move topRotationPriveMove = new Move();
      topRotationPriveMove.setName(getMoveName(0, 'U', -1));
      topRotationPriveMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, topRotationPriveMove.getName())
              .fillFrontFace().setRotation('U', true).getImage());
      topRotationPriveMove.getLoops().addAll(faceReverseTurn(top, width, depth));
      topRotationPriveMove.getLoops().addAll(faceTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        topRotationPriveMove.getLoops()
            .addAll(topSideReverseTurn(layer, width, height, depth, left, right, front, back));
      }
      topRotationPriveMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(topRotationPriveMove);
    }

    // Right face moves (if depth equals height, we can do 90° rotations)
    if (depth == height) {
      // Create R, R', L, L', x, x' moves
      for (int deep = 1; deep < width; deep++) {
        // Create R move (clockwise right face rotation)
        log.info("Generating R to depth {}", deep);
        Move rightMove = new Move();
        rightMove.setName(getMoveName(deep, 'R', 1));
        rightMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, rightMove.getName()).fillFromRight(deep)
                .setRotation('R', false).getImage());
        rightMove.getLoops().addAll(faceTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          rightMove.getLoops()
              .addAll(rightSideTurn(layer, width, height, depth, front, back, top, bottom));
        }
        rightMove.setCost(1);
        puzzle.addMove(rightMove);

        // Create R' move (counterclockwise right face rotation)
        log.info("Generating R' to depth {}", deep);
        Move rightPrimeMove = new Move();
        rightPrimeMove.setName(getMoveName(deep, 'R', -1));
        rightPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, rightPrimeMove.getName())
                .fillFromRight(deep).setRotation('R', true).getImage());
        rightPrimeMove.getLoops().addAll(faceReverseTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          rightPrimeMove.getLoops()
              .addAll(rightSideReverseTurn(layer, width, height, depth, front, back, top, bottom));
        }
        rightPrimeMove.setCost(1);
        puzzle.addMove(rightPrimeMove);

        // Create L move (clockwise left face rotation)
        log.info("Generating L to depth {}", deep);
        Move leftMove = new Move();
        leftMove.setName(getMoveName(deep, 'L', 1));
        leftMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, leftMove.getName()).fillFromLeft(deep)
                .setRotation('L', false).getImage());
        leftMove.getLoops().addAll(faceTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          leftMove.getLoops().addAll(
              rightSideReverseTurn(width - layer + 1, width, height, depth, front, back, top,
                  bottom));
        }
        leftMove.setCost(1);
        puzzle.addMove(leftMove);

        // Create L' move (counterclockwise left face rotation)
        log.info("Generating L' to depth {}", deep);
        Move leftPrimeMove = new Move();
        leftPrimeMove.setName(getMoveName(deep, 'L', -1));
        leftPrimeMove.setImageIcon(
            CuboidMoveIconBuilder.builer(width, height, leftPrimeMove.getName()).fillFromLeft(deep)
                .setRotation('L', true).getImage());
        leftPrimeMove.getLoops().addAll(faceReverseTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          leftPrimeMove.getLoops().addAll(
              rightSideTurn(width - layer + 1, width, height, depth, front, back, top, bottom));
        }
        leftPrimeMove.setCost(1);
        puzzle.addMove(leftPrimeMove);
      }

      // Create x move (whole puzzle rotation around left-right axis, no cost)
      log.info("Generating x");
      Move rightRotationMove = new Move();
      rightRotationMove.setName(getMoveName(0, 'R', 1));
      rightRotationMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, rightRotationMove.getName()).fillFrontFace()
              .setRotation('R', false).getImage());
      rightRotationMove.getLoops().addAll(faceTurn(right, depth, height));
      rightRotationMove.getLoops().addAll(faceReverseTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        rightRotationMove.getLoops()
            .addAll(rightSideTurn(layer, width, height, depth, front, back, top, bottom));
      }
      rightRotationMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(rightRotationMove);

      // Create x' move (inverse whole puzzle rotation, no cost)
      log.info("Generating x'");
      Move rightRotationPriveMove = new Move();
      rightRotationPriveMove.setName(getMoveName(0, 'R', -1));
      rightRotationPriveMove.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, rightRotationPriveMove.getName())
              .fillFrontFace().setRotation('R', true).getImage());
      rightRotationPriveMove.getLoops().addAll(faceReverseTurn(right, depth, height));
      rightRotationPriveMove.getLoops().addAll(faceTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        rightRotationPriveMove.getLoops()
            .addAll(rightSideReverseTurn(layer, width, height, depth, front, back, top, bottom));
      }
      rightRotationPriveMove.setCost(0); // Free move since it's a whole puzzle rotation
      puzzle.addMove(rightRotationPriveMove);
    }

    // Double turns (180° rotations) - these are always possible regardless of dimensions

    // Front-back axis double turns
    for (int deep = 1; deep < depth; deep++) {
      // F2 moves (180° front face rotation)
      log.info("Generating F2");
      Move f2Move = new Move();
      f2Move.setName(getMoveName(deep, 'F', 2));
      f2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, f2Move.getName()).fillFrontFace()
              .setRotation('F', false).getImage());
      f2Move.getLoops().addAll(faceDoubleTurn(front, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        f2Move.getLoops()
            .addAll(frontSideDoubleTurn(layer, width, height, depth, left, right, top, bottom));
      }
      f2Move.setCost(1); // 180° rotation counts as 1 move in OBTM
      puzzle.addMove(f2Move);

      // B2 moves (180° back face rotation)
      log.info("Generating B2");
      Move b2Move = new Move();
      b2Move.setName(getMoveName(deep, 'B', 2));
      b2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, b2Move.getName()).setRotation('B', false)
              .getImage());
      b2Move.getLoops().addAll(faceDoubleTurn(back, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        b2Move.getLoops().addAll(
            frontSideDoubleTurn(depth - layer + 1, width, height, depth, left, right, top, bottom));
      }
      b2Move.setCost(1);
      puzzle.addMove(b2Move);
    }

    // z2 move (180° whole puzzle rotation around front-back axis, no cost)
    log.info("Generating z2");
    Move z2Move = new Move();
    z2Move.setName(getMoveName(0, 'F', 2));
    z2Move.setImageIcon(
        CuboidMoveIconBuilder.builer(width, height, z2Move.getName()).fillFrontFace()
            .setRotation('F', false).getImage());
    z2Move.getLoops().addAll(faceDoubleTurn(front, width, height));
    z2Move.getLoops().addAll(faceDoubleTurn(back, width, height));
    for (int layer = 1; layer <= depth; layer++) {
      z2Move.getLoops()
          .addAll(frontSideDoubleTurn(layer, width, height, depth, left, right, top, bottom));
    }
    z2Move.setCost(0); // Free move since it's a whole puzzle rotation
    puzzle.addMove(z2Move);

    // Top-bottom axis double turns
    for (int deep = 1; deep < height; deep++) {
      // U2 moves (180° top face rotation)
      log.info("Generating U2 to depth {}", deep);
      Move u2Move = new Move();
      u2Move.setName(getMoveName(deep, 'U', 2));
      u2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, u2Move.getName()).fillFromTop(deep)
              .setRotation('U', false).getImage());
      u2Move.getLoops().addAll(faceDoubleTurn(top, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        u2Move.getLoops()
            .addAll(topSideDoubleTurn(layer, width, height, depth, right, left, front, back));
      }
      u2Move.setCost(1);
      puzzle.addMove(u2Move);

      // D2 moves (180° bottom face rotation)
      log.info("Generating D2 to depth {}", deep);
      Move d2Move = new Move();
      d2Move.setName(getMoveName(deep, 'D', 2));
      d2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, d2Move.getName()).fillFromBottom(deep)
              .setRotation('D', false).getImage());
      d2Move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        d2Move.getLoops().addAll(
            topSideDoubleTurn(height - layer + 1, width, height, depth, right, left, front, back));
      }
      d2Move.setCost(1);
      puzzle.addMove(d2Move);
    }

    // y2 move (180° whole puzzle rotation around top-bottom axis, no cost)
    log.info("Generating y2");
    Move y2Move = new Move();
    y2Move.setName(getMoveName(0, 'U', 2));
    y2Move.setImageIcon(
        CuboidMoveIconBuilder.builer(width, height, y2Move.getName()).fillFrontFace()
            .setRotation('U', false).getImage());
    y2Move.getLoops().addAll(faceDoubleTurn(top, width, depth));
    y2Move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
    for (int layer = 1; layer <= height; layer++) {
      y2Move.getLoops()
          .addAll(topSideDoubleTurn(layer, width, height, depth, right, left, front, back));
    }
    y2Move.setCost(0); // Free move since it's a whole puzzle rotation
    puzzle.addMove(y2Move);

    // Left-right axis double turns
    for (int deep = 1; deep < width; deep++) {
      // R2 moves (180° right face rotation)
      log.info("Generating R2 to depth {}", deep);
      Move r2Move = new Move();
      r2Move.setName(getMoveName(deep, 'R', 2));
      r2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, r2Move.getName()).fillFromRight(deep)
              .setRotation('R', false).getImage());
      r2Move.getLoops().addAll(faceDoubleTurn(right, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        r2Move.getLoops()
            .addAll(rightSideDoubleTurn(layer, width, height, depth, front, back, top, bottom));
      }
      r2Move.setCost(1);
      puzzle.addMove(r2Move);

      // L2 moves (180° left face rotation)
      log.info("Generating L2 to depth {}", deep);
      Move l2Move = new Move();
      l2Move.setName(getMoveName(deep, 'L', 2));
      l2Move.setImageIcon(
          CuboidMoveIconBuilder.builer(width, height, l2Move.getName()).fillFromLeft(deep)
              .setRotation('L', false).getImage());
      l2Move.getLoops().addAll(faceDoubleTurn(left, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        l2Move.getLoops().addAll(
            rightSideDoubleTurn(width - layer + 1, width, height, depth, front, back, top, bottom));
      }
      l2Move.setCost(1);
      puzzle.addMove(l2Move);
    }

    // x2 move (180° whole puzzle rotation around left-right axis, no cost)
    log.info("Generating x2");
    Move x2Move = new Move();
    x2Move.setName(getMoveName(0, 'R', 2));
    x2Move.setImageIcon(
        CuboidMoveIconBuilder.builer(width, height, x2Move.getName()).setRotation('R', false)
            .getImage());
    x2Move.getLoops().addAll(faceDoubleTurn(right, depth, height));
    x2Move.getLoops().addAll(faceDoubleTurn(left, depth, height));
    for (int layer = 1; layer <= width; layer++) {
      x2Move.getLoops()
          .addAll(rightSideDoubleTurn(layer, width, height, depth, front, back, top, bottom));
    }
    x2Move.setCost(0); // Free move since it's a whole puzzle rotation
    puzzle.addMove(x2Move);

    // Center the puzzle in the rendering area
    puzzle.centerPuzzle();

    // Initialize the puzzle's base state
    puzzle.takeSnapshot();

    log.info("Finished building puzzle with {} cells, {} compiled moves", puzzle.getCells().size(),
        puzzle.getCompiledMoves().size());
    return puzzle;
  }

  /**
   * Creates a grid of cells at the specified position with the given color and scale.
   *
   * @param x      The x-coordinate of the top-left corner of the grid
   * @param y      The y-coordinate of the top-left corner of the grid
   * @param w      The width of the grid in cells
   * @param h      The height of the grid in cells
   * @param colour The color to assign to all cells in the grid
   * @param scale  The scale factor for the cells
   * @return A list of Cell objects forming the grid
   */
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
        cells.add(c);
      }
    }
    return cells;
  }

  /**
   * Generates a move name based on the depth, face, and number of quarter turns. Handles special
   * cases for cube rotations when depth is 0.
   *
   * @param depth        The depth of the move (0 for whole cube rotation, 1 for outer face, etc.)
   * @param face         The face being rotated ('F', 'B', 'U', 'D', 'L', 'R')
   * @param quarterTurns The number of quarter turns (1 for 90°, 2 for 180°, -1 for -90°)
   * @return A standardized move name following cube notation conventions
   */
  public static String getMoveName(int depth, char face, int quarterTurns) {
    StringBuilder result = new StringBuilder();

    // For moves deeper than 2 layers, prefix with depth number
    if (depth > 2) { // NOPMD - AvoidLiteralsInIfCondition: literal value 2 is clear in context
      result.append(depth);
    }

    // Face designation
    if (depth > 0) {
      // Regular face move
      result.append(face);
    } else {
      // Whole cube rotation
      switch (face) {
        case 'R':
          result.append("x"); // Right face corresponds to x rotation
          break;
        case 'U':
          result.append("y"); // Up face corresponds to y rotation
          break;
        case 'F':
          result.append("z"); // Front face corresponds to z rotation
          break;
        default:
          result.append("?");
      }
    }

    // For moves with multiple layers but not the whole puzzle, add 'w' for wide
    if (depth > 1) { // NOPMD - AvoidLiteralsInIfCondition: literal value 1 is clear in context
      result.append("w");
    }

    // Rotation indicator
    if (quarterTurns
        == 2) { // NOPMD - AvoidLiteralsInIfCondition: literal values are meaningful for cube
      // notation
      result.append("2"); // 180° turn
    } else if (quarterTurns
        == -1) { // NOPMD - AvoidLiteralsInIfCondition: literal values are meaningful for cube
      // notation
      result.append("'"); // Counterclockwise turn
    }
    // Default clockwise has no suffix

    return result.toString();
  }

  /**
   * Creates move loops for a 90° rotation of a face. Implements clockwise rotation of the entire
   * face.
   *
   * @param face   The list of cells on the face to rotate
   * @param width  The width of the face
   * @param height The height of the face
   * @return A list of move loops defining the face rotation
   */
  private List<MoveLoop> faceTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = new ArrayList<>();

    // Process each ring from outer to inner
    for (int dx = 0; dx < divRoundUp(width, 2); dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        MoveLoop loop = new MoveLoop();
        // Add cells in clockwise order
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get((width - dy - 1) + width * dx));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        loop.getCells().add(face.get(dy + width * (height - dx - 1)));
        result.add(loop);
      }
    }
    return result;
  }

  /**
   * Creates move loops for a front-face turn that affects side layers. Used for F, Fw, 3F, etc.
   * moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param left   The cells on the left face
   * @param right  The cells on the right face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the transformation
   */
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

  /**
   * Creates move loops for a 90° counter-clockwise rotation of a face. Reverses the loops created
   * by faceTurn.
   *
   * @param face   The list of cells on the face to rotate
   * @param width  The width of the face
   * @param height The height of the face
   * @return A list of move loops defining the reverse face rotation
   */
  private List<MoveLoop> faceReverseTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = faceTurn(face, width, height);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  /**
   * Creates reverse move loops for a front-face turn. Used for F', Fw', 3F', etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param left   The cells on the left face
   * @param right  The cells on the right face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the reverse transformation
   */
  private List<MoveLoop> frontSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = frontSideTurn(layer, width, height, depth, left, right, top, bottom);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  /**
   * Creates move loops for a top-face turn that affects side layers. Used for U, Uw, 3U, etc.
   * moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param left   The cells on the left face
   * @param right  The cells on the right face
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @return A list of move loops defining the transformation
   */
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

  /**
   * Creates reverse move loops for a top-face turn. Used for U', Uw', 3U', etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param left   The cells on the left face
   * @param right  The cells on the right face
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @return A list of move loops defining the reverse transformation
   */
  private List<MoveLoop> topSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> front, List<Cell> back) {
    List<MoveLoop> result = topSideTurn(layer, width, height, depth, left, right, front, back);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  /**
   * Creates move loops for a right-face turn that affects side layers. Used for R, Rw, 3R, etc.
   * moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the transformation
   */
  private List<MoveLoop> rightSideTurn(int layer, int width, int height, int depth,
      List<Cell> front, List<Cell> back, List<Cell> top, List<Cell> bottom) {
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

  /**
   * Creates reverse move loops for a right-face turn. Used for R', Rw', 3R', etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the reverse transformation
   */
  private List<MoveLoop> rightSideReverseTurn(int layer, int width, int height, int depth,
      List<Cell> front, List<Cell> back, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = rightSideTurn(layer, width, height, depth, front, back, top, bottom);
    for (MoveLoop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  /**
   * Creates move loops for a 180° rotation of a face. Connects opposite cells in the face.
   *
   * @param face   The list of cells on the face to rotate
   * @param width  The width of the face
   * @param height The height of the face
   * @return A list of move loops defining the 180° face rotation
   */
  private List<MoveLoop> faceDoubleTurn(List<Cell> face, int width, int height) {
    List<MoveLoop> result = new ArrayList<>();

    // Connect opposite cells in each row and column
    for (int dx = 0; dx < width; dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        MoveLoop loop = new MoveLoop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        result.add(loop);
      }
    }

    // Special case for odd-numbered heights - connect cells in middle row
    if (height % 2
        == 1) { // NOPMD - AvoidLiteralsInIfCondition: literal values are clear for modulo
      // arithmetic
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

  /**
   * Creates move loops for a 180° front-face turn that affects side layers. Used for F2, Fw2, 3F2,
   * etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param left   The cells on the left face
   * @param right  The cells on the right face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the 180° transformation
   */
  private List<MoveLoop> frontSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> left, List<Cell> right, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = new ArrayList<>();

    // Connect opposite cells on top and bottom faces
    for (int i = 0; i < width; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(top.get(i + (depth - layer) * width));
      l.getCells().add(bottom.get(width - i - 1 + (layer - 1) * width));
      result.add(l);
    }

    // Connect opposite cells on left and right faces
    for (int i = 0; i < height; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(right.get(layer - 1 + i * depth));
      l.getCells().add(left.get(depth - layer + (height - i - 1) * depth));
      result.add(l);
    }
    return result;
  }

  /**
   * Creates move loops for a 180° top-face turn that affects side layers. Used for U2, Uw2, 3U2,
   * etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param right  The cells on the right face
   * @param left   The cells on the left face
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @return A list of move loops defining the 180° transformation
   */
  private List<MoveLoop> topSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> right, List<Cell> left, List<Cell> front, List<Cell> back) {
    List<MoveLoop> result = new ArrayList<>();

    // Connect opposite cells on front and back faces
    for (int i = 0; i < width; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(front.get(i + (layer - 1) * width));
      l.getCells().add(back.get(i + (layer - 1) * width));
      result.add(l);
    }

    // Connect opposite cells on left and right faces
    for (int i = 0; i < depth; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(right.get(i + (layer - 1) * depth));
      l.getCells().add(left.get(i + (layer - 1) * depth));
      result.add(l);
    }
    return result;
  }

  /**
   * Creates move loops for a 180° right-face turn that affects side layers. Used for R2, Rw2, 3R2,
   * etc. moves.
   *
   * @param layer  The layer depth from the face (1-based)
   * @param width  The width of the puzzle
   * @param height The height of the puzzle
   * @param depth  The depth of the puzzle
   * @param front  The cells on the front face
   * @param back   The cells on the back face
   * @param top    The cells on the top face
   * @param bottom The cells on the bottom face
   * @return A list of move loops defining the 180° transformation
   */
  private List<MoveLoop> rightSideDoubleTurn(int layer, int width, int height, int depth,
      List<Cell> front, List<Cell> back, List<Cell> top, List<Cell> bottom) {
    List<MoveLoop> result = new ArrayList<>();

    // Connect opposite cells on front and back faces
    for (int i = 0; i < height; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(front.get(i * width + width - layer));
      l.getCells().add(back.get((height - i - 1) * width + layer - 1));
      result.add(l);
    }

    // Connect opposite cells on top and bottom faces
    for (int i = 0; i < depth; i++) {
      MoveLoop l = new MoveLoop();
      l.getCells().add(top.get(i * width + width - layer));
      l.getCells().add(bottom.get(i * width + width - layer));
      result.add(l);
    }
    return result;
  }

  /**
   * Calculates the ceiling division of two integers. Used for determining loop bounds in face
   * rotation algorithms.
   *
   * @param num     The numerator
   * @param divisor The divisor
   * @return The ceiling of num/divisor
   */
  public static int divRoundUp(int num, int divisor) {
    return (num + divisor - 1) / divisor;
  }
}
