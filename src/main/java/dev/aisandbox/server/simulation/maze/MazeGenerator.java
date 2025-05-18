/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.maze;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class MazeGenerator {

  /**
   * Generates a maze based on the specified parameters.
   *
   * @param size     The dimensions of the maze (width, height, zoom level).
   * @param mazeType The type of maze to generate (e.g. BINARYTREE, SIDEWINDER, BRAIDED,
   *                 RECURSIVEBACKTRACKER).
   * @param rand     A random number generator used for generating the maze.
   * @return The generated maze.
   */
  public static Maze generateMaze(MazeSize size, MazeType mazeType, Random rand) {
    Maze maze = new Maze(size.getWidth(), size.getHeight(), size.getZoomLevel());
    switch (mazeType) {
      case BINARYTREE -> applyBinaryTree(rand, maze);
      case SIDEWINDER -> applySidewinder(rand, maze);
      case BRAIDED -> {
        applyRecursiveBacktracker(rand, maze);
        removeDeadEnds(rand, maze);
      }
      case RECURSIVEBACKTRACKER -> applyRecursiveBacktracker(rand, maze);
    }
    findFurthestPoints(maze);
    return maze;
  }

  /**
   * Applies the Binary Tree algorithm to a given maze.
   * <p>
   * The Binary Tree algorithm is a simple method for generating mazes. It works by selecting a
   * random cell and then creating paths from that cell to its unvisited neighbors, repeating this
   * process until all cells have been visited.
   *
   * @param rand A random number generator used for generating the maze.
   * @param maze The maze to apply the Binary Tree algorithm to.
   */
  public static void applyBinaryTree(Random rand, Maze maze) {
    log.debug("Applying binary tree to maze");
    for (Cell c : maze.getCellList()) {
      List<Cell> targets = new ArrayList<>();
      if (c.getNeighbours().get(Direction.EAST) != null) {
        targets.add(c.getNeighbours().get(Direction.EAST));
      }
      if (c.getNeighbours().get(Direction.NORTH) != null) {
        targets.add(c.getNeighbours().get(Direction.NORTH));
      }
      if (!targets.isEmpty()) {
        c.addPath(targets.get(rand.nextInt(targets.size())));
      }
    }
    log.info("finished binary tree");
  }

  /**
   * Applies the Sidewinder algorithm to a given maze.
   * <p>
   * The Sidewinder algorithm is a simple method for generating mazes. It works by starting at the
   * top row of the maze and moving rightwards, creating paths as it goes. When the end of the
   * current cell is reached, the algorithm moves down to the next cell in the top row.
   *
   * @param rand A random number generator used for generating the maze.
   * @param maze The maze to apply the Sidewinder algorithm to.
   */
  public static void applySidewinder(Random rand, Maze maze) {
    log.debug("Applying sidewinder to maze");
    // special case, join the top row
    for (int x = 0; x < maze.getWidth() - 1; x++) {
      maze.getCellArray()[x][0].addPath(Direction.EAST);
    }
    for (int y = 1; y < maze.getHeight(); y++) {
      List<Cell> group = new ArrayList<>();
      for (int x = 0; x < maze.getWidth(); x++) {
        // get cell
        Cell c = maze.getCellArray()[x][y];
        // add cell to group
        if (!group.isEmpty()) {
          c.addPath(Direction.WEST);
        }
        group.add(c);
        if ((x == maze.getWidth() - 1) || rand.nextBoolean()) {
          // link upwards
          Cell c2 = group.get(rand.nextInt(group.size()));
          c2.addPath(Direction.NORTH);
          group.clear();
        }
      }
    }
    log.debug("Finished sidewinder");
  }

  /**
   * Applies the Recursive Backtracker algorithm to a given maze.
   * <p>
   * The Recursive Backtracker algorithm is a depth-first search method for generating mazes. It
   * works by starting at a random cell and then recursively visiting its unvisited neighbors,
   * creating paths as it goes.
   *
   * @param rand A random number generator used for generating the maze.
   * @param maze The maze to apply the Recursive Backtracker algorithm to.
   */
  public static void applyRecursiveBacktracker(Random rand, Maze maze) {
    log.debug("Applying recursive backtracker");
    List<Cell> stack = new ArrayList<>();
    List<Cell> unvisited = new ArrayList<>(maze.getCellList());
    stack.add(unvisited.remove(rand.nextInt(unvisited.size())));
    while (!stack.isEmpty()) {
      // copy the last entry off the stack
      Cell current = stack.get(stack.size() - 1);
      // work out the unvisited neighbours
      List<Cell> neighbours = new ArrayList<>();
      for (Cell n : current.getNeighbours().values()) {
        if (unvisited.contains(n)) {
          neighbours.add(n);
        }
      }
      if (neighbours.isEmpty()) {
        // no neighbours - remove from stack and step backwards
        stack.remove(current);
      } else {
        // pick random neighbour - link, then add this to the stack
        Cell next = neighbours.get(rand.nextInt(neighbours.size()));
        current.addPath(next);
        stack.add(next);
        unvisited.remove(next);
      }
    }
    log.debug("Finished backtracker");
  }

  /**
   * Removes dead ends from a given maze.
   * <p>
   * A dead end is a cell with only one path leading out of it. This method works by checking each
   * cell in the maze and, if it has only one path, selecting a random neighbor to create a new path
   * to.
   *
   * @param rand A random number generator used for generating the maze.
   * @param maze The maze to remove dead ends from.
   */
  public static void removeDeadEnds(Random rand, Maze maze) {
    // check each cell
    for (Cell current : maze.getCellList()) {
      // work out how many paths if less than two, add a new one
      if (current.getPaths().size() < 2) {
        List<Cell> target = new ArrayList<>();
        for (Direction d : current.getNeighbours().keySet()) {
          if (!current.isPath(d)) {
            target.add(current.getNeighbours().get(d));
          }
        }
        if (!target.isEmpty()) {
          current.addPath(target.get(rand.nextInt(target.size())));
        }
      }
    }
  }

  /**
   * Finds the furthest points in a given maze.
   * <p>
   * This method works by applying the Dijkstra algorithm to the maze, first selecting a random
   * start cell and then finding the cell with the highest value (i.e. the furthest point from the
   * start). The maze is then normalized so that all values are between 0 and 1.
   *
   * @param maze The maze to find the furthest points in.
   */
  public static void findFurthestPoints(Maze maze) {
    applyDijkstra(maze);
    Cell start = getHighestVelueCell(maze);
    maze.setStartCell(start);
    applyDijkstra(maze, start);
    Cell finish = getHighestVelueCell(maze);
    maze.setEndCell(finish);
    normalise(maze);
  }

  /**
   * Applies the Dijkstra algorithm to a given maze.
   * <p>
   * The Dijkstra algorithm is a method for finding the shortest path between two points in a graph.
   * In this implementation, it is used to assign a value to each cell based on its distance from a
   * random start point.
   *
   * @param maze The maze to apply the Dijkstra algorithm to.
   */
  public static void applyDijkstra(Maze maze) {
    log.info("Applying dijkstra - picking random start cell from maze with {} cells",
        maze.getCellList().size());
    Random rand = new Random(System.currentTimeMillis());
    applyDijkstra(maze, maze.getCellList().get(rand.nextInt(maze.getCellList().size())));
  }

  /**
   * Finds the cell with the highest value in a given maze.
   * <p>
   * This method works by iterating over each cell in the maze and returning the one with the
   * highest value.
   *
   * @param maze The maze to find the furthest point in.
   * @return A Cell object representing the furthest point from the start.
   */
  public static Cell getHighestVelueCell(Maze maze) {
    Cell result = null;
    float v = Float.MIN_VALUE;
    for (Cell c : maze.getCellList()) {
      if (c.getValue() > v) {
        v = c.getValue();
        result = c;
      }
    }
    return result;
  }

  /**
   * Applies the Dijkstra algorithm to a given maze.
   * <p>
   * The Dijkstra algorithm is a method for finding the shortest path between two points in a graph.
   * In this implementation, it is used to assign a value to each cell based on its distance from a
   * random start point.
   *
   * @param maze  The maze to apply the Dijkstra algorithm to.
   * @param start A Cell object representing the starting point.
   */
  public static void applyDijkstra(Maze maze, Cell start) {
    // list of cells that have not been visited
    Set<Cell> unvisitedList = new HashSet<>();
    // the current round of cells
    List<Cell> currentList = new ArrayList<>();
    // the next round of cells
    List<Cell> nextList = new ArrayList<>();
    // add cells to the unvisited list
    unvisitedList.addAll(maze.getCellList());
    // pick our first (random cell)
    currentList.add(start);
    unvisitedList.remove(start);
    int step = 0;
    while (!currentList.isEmpty()) {
      for (Cell base : currentList) {
        // assign a value
        base.setValue(step);
        // add neighbours in next list
        for (Direction d : base.getPaths()) {
          Cell c2 = base.getNeighbours().get(d);
          if (unvisitedList.contains(c2)) {
            // mark this for the next round
            nextList.add(c2);
            unvisitedList.remove(c2);
          }
        }
      }
      // processed all in current list - move on
      currentList = nextList;
      nextList = new ArrayList<>();
      step++;
    }
  }

  /**
   * Normalizes the values of each cell in a given maze.
   * <p>
   * This method works by finding the minimum and maximum values in the maze, then scaling all
   * values to be between 0 and 1.
   *
   * @param maze The maze to normalize.
   */
  public static void normalise(Maze maze) {
    // work our max and min
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
    for (Cell c : maze.getCellList()) {
      max = Math.max(c.getValue(), max);
      min = Math.min(c.getValue(), min);
    }
    // scale all values between 0 and 1
    for (Cell c : maze.getCellList()) {
      c.setValue((c.getValue() - min) / (max - min));
    }
  }

}
