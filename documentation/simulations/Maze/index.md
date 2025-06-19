# Maze Scenario

The Maze scenario is a classic problem in path finding and reinforcement learning. The agent is
placed in a two-dimensional maze and must find its way to an exit. Once the exit is found, the agent
is repositioned randomly within the maze and must find the exit again. This cycle continues until
the episode ends.

This scenario tests the agent's ability to learn spatial navigation, path finding, and memory. As
the agent explores the maze, it can build up a mental model of the environment and optimize its path
to find the most efficient route to the exit.

In some cases, the maze construction will introduce biases that can be learned and exploited.

# Goal

Write an AI that can navigate through a maze environment, find the exit, and optimize paths to
maximize rewards within the limited number of available moves.

# Algorithms and Hints

Consider implementing algorithms such as:

- Random exploration for initial maze discovery
- Depth-first search or breadth-first search for systematic exploration
- A* or Dijkstra's algorithm for finding optimal paths once the maze layout is known
- Memory systems to track visited locations and build a map of the maze

# Setup

At the start of each episode, the agent is placed in a maze with a defined size and structure. The
agent must navigate through the maze to find the exit. When the exit is found, the agent is
repositioned randomly within the maze.

The following options are available when setting up the scenario:

| Key      | Value                                                                                              |
|----------|----------------------------------------------------------------------------------------------------|
| mazeSize | The size of the maze (SMALL, MEDIUM, LARGE)                                                        |
| mazeType | The style/generation algorithm of the maze (BINARYTREE, SIDEWINDER, RECURSIVEBACKTRACKER, BRAIDED) |

# Protocol

The protocol is detailed
in [Maze.proto](https://github.com/graham-evans/AISandbox-Server/blob/main/src/main/proto/Maze.proto)
and follows a standard State -> Action -> Result pattern.

## Maze State

| Component | Data Type | Description                                          |
|-----------|-----------|------------------------------------------------------|
| sessionID | string    | Unique identifier for this simulation run            |
| episodeID | string    | Unique identifier for the current episode            |
| startX    | int32     | The horizontal (x) coordinate of the agent           |
| startY    | int32     | The vertical (y) coordinate of the agent             |
| movesLeft | int32     | Number of moves remaining before the episode ends    |
| width     | int32     | The width of the maze (number of cells horizontally) |
| height    | int32     | The height of the maze (number of cells vertically)  |

## Maze Action

| Component | Data Type | Description                                                                 |
|-----------|-----------|-----------------------------------------------------------------------------|
| direction | Direction | The direction in which the agent chooses to move (NORTH, SOUTH, EAST, WEST) |

## Maze Result

| Component        | Data Type | Description                                        |
|------------------|-----------|----------------------------------------------------|
| startX           | int32     | The x position before movement                     |
| startY           | int32     | The y position before movement                     |
| direction        | Direction | The direction the agent moved                      |
| endX             | int32     | The x position after movement                      |
| endY             | int32     | The y position after movement                      |
| stepScore        | double    | The score received for this specific move          |
| accumulatedScore | double    | The total score accumulated so far in this episode |