package dev.aisandbox.server.simulation.maze;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@UtilityClass
public class MazeGenerator {

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
     * applyBinaryTree.
     *
     * @param rand a {@link java.util.Random} object.
     * @param maze a {@link Maze} object.
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
     * applyRecursiveBacktracker.
     *
     * @param rand a {@link java.util.Random} object.
     * @param maze a {@link Maze} object.
     */
    public static void applyRecursiveBacktracker(Random rand, Maze maze) {
        log.debug("Applying recursive backtracker");
        List<Cell> stack = new ArrayList<>();
        List<Cell> unvisited = new ArrayList<>();
        unvisited.addAll(maze.getCellList());
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
     * removeDeadEnds.
     *
     * @param rand a {@link java.util.Random} object.
     * @param maze a {@link Maze} object.
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
     * findFurthestPoints.
     *
     * @param maze a {@link Maze} object.
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
     * Apply the Dijkstra algorithm, once, to the maze. This will result in the <i>value</i> of each
     * cell growing from 0 (at a random point)
     *
     * @param maze a {@link Maze} object.
     */
    public static void applyDijkstra(Maze maze) {
        log.info(
                "Applying dijkstra - picking random start cell from maze with {} cells",
                maze.getCellList().size());
        Random rand = new Random(System.currentTimeMillis());
        applyDijkstra(maze, maze.getCellList().get(rand.nextInt(maze.getCellList().size())));
    }

    /**
     * Apply the Dijkstra algorithm, once, to the maze. This will result in the <i>value</i> of each
     * cell growing from the starting position
     *
     * @param maze  a {@link Maze} object.
     * @param start a {@link Cell} object.
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
     * getHighestVelueCell.
     *
     * @param maze a {@link Maze} object.
     * @return a {@link Cell} object.
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
     * update the values of each cell - distributing them between 0.0 and 1.0
     *
     * @param maze a {@link Maze} object.
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
