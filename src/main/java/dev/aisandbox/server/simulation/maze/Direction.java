package dev.aisandbox.server.simulation.maze;

public enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public static Direction fromProto(dev.aisandbox.server.simulation.maze.proto.Direction direction) {
        return switch (direction) {
            case NORTH, UNRECOGNIZED -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    /**
     * Return the direction opposite to the current value (i.e. North -> South)
     *
     * @return a {@link Direction} object opposite to this one.
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }
}
