package dev.aisandbox.server.simulation.maze;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;


@RequiredArgsConstructor
public class Cell {

    @Getter
    private final int positionX;
    @Getter
    private final int positionY;

    @Getter
    EnumMap<Direction, Cell> neighbours = new EnumMap<>(Direction.class);

    @Getter
    Set<Direction> paths = new HashSet<>();

    @Getter
    @Setter
    private float value;

    /**
     * Link this cell with a destination in the given direction
     *
     * @param d a {@link Direction} object.
     * @param c a {@link Cell} object.
     */
    public void link(Direction d, Cell c) {
        if (c != null) {
            neighbours.put(d, c);
        }
    }

    /**
     * Link this cell with a destination in the given direction, and link that back to this.
     *
     * @param d a {@link Direction} object.
     * @param c a {@link Cell} object.
     */
    public void linkBi(Direction d, Cell c) {
        if (c != null) {
            neighbours.put(d, c);
            c.getNeighbours().put(d.opposite(), this);
        }
    }

    /**
     * getLinks.
     *
     * @return a {@link java.util.Collection} object.
     */
    public java.util.Collection<Cell> getLinks() {
        return neighbours.values();
    }

    /**
     * Can you move from this cell to the target cell
     *
     * @param c a {@link Cell} object.
     * @return a boolean.
     */
    public boolean isLinked(Cell c) {
        return neighbours.containsValue(c);
    }

    /**
     * remove the link between this and another cell.
     */
    public void disconnect() {
        Iterator<Map.Entry<Direction, Cell>> itr = neighbours.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Direction, Cell> entry = itr.next();
            entry.getValue().getNeighbours().remove(entry.getKey().opposite(), this);
            itr.remove();
        }
    }

    /**
     * addPath.
     *
     * @param direction a {@link Direction} object.
     */
    public void addPath(Direction direction) {
        paths.add(direction);
        getNeighbours().get(direction).getPaths().add(direction.opposite());
    }

    /**
     * isPath.
     *
     * @param direction a {@link Direction} object.
     * @return a boolean.
     */
    public boolean isPath(Direction direction) {
        return paths.contains(direction);
    }

    /**
     * addPath.
     *
     * @param c a {@link Cell} object.
     */
    public void addPath(Cell c) {
        Iterator<Map.Entry<Direction, Cell>> itr = neighbours.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Direction, Cell> entry = itr.next();
            if (entry.getValue() == c) {
                addPath(entry.getKey());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cell cell = (Cell) o;
        return positionX == cell.positionX && positionY == cell.positionY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(positionX, positionY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Cell{"
                + "positionX="
                + positionX
                + ", positionY="
                + positionY
                + ", paths="
                + paths
                + ", value="
                + value
                + '}';
    }
}
