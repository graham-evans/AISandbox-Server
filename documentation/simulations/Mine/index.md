# Mine Hunter

Mine Hunter pits the AI against a minefield! A known quantity of mines have been distributed across
a grid of squares and the AI agent must work out where they are. To help, each uncovered square will
show how many mines are in the surrounding squares.

This scenario is similar to the classic Minesweeper game, where the objective is to uncover all
non-mine cells without triggering any mines.

# Goal

Write an AI that can successfully locate all mines in the grid by using logical deduction based on
the numerical clues provided when uncovering cells.

# Algorithms and Hints

The most basic approach is to implement a rule-based solver that:

1. Uncovers cells that are guaranteed to be safe based on surrounding clues
2. Flags cells that are guaranteed to contain mines based on surrounding clues
3. When no guaranteed moves are available, uses probability estimation to make the safest choice

More advanced approaches might include:

- Constraint satisfaction algorithms
- Pattern recognition for common mine configurations
- Probabilistic inference to determine the likelihood of mines in various locations

# Setup

At the start of each round, mines are randomly distributed across the grid according to the selected
board size.

The following size configurations are available:

| Size   | Grid Dimensions | Number of Mines |
|--------|-----------------|-----------------|
| SMALL  | 9x9             | 10              |
| MEDIUM | 16x16           | 40              |
| LARGE  | 24x24           | 99              |
| MEGA   | 40x40           | 150             |

The AI agent must uncover cells while avoiding mines. Each uncovered cell reveals a number
indicating how many mines are in the adjacent cells (including diagonals). The AI can also place
flags to mark cells it believes contain mines.

# Protocol

The protocol is detailed
in [Mine.proto](https://github.com/graham-evans/AISandbox-Server/blob/main/src/main/proto/Mine.proto)
and follows a request-response pattern where the agent receives the current state, makes a move, and
then receives the result of that move.

## Mine State

| Component | Data Type       | Description                                                                                   |
|-----------|-----------------|-----------------------------------------------------------------------------------------------|
| sessionID | string          | Unique identifier for this simulation run                                                     |
| episodeID | string          | Unique identifier for the current episode                                                     |
| width     | int32           | The width of the game board (number of cells horizontally)                                    |
| height    | int32           | The height of the game board (number of cells vertically)                                     |
| flagsLeft | int32           | The number of flags the player has remaining to place                                         |
| row       | repeated string | The current visible state of each row on the board (may contain hidden cells, numbers, flags) |

## Mine Action

| Component | Data Type  | Description                                       |
|-----------|------------|---------------------------------------------------|
| x         | int32      | The x-coordinate of the target cell (horizontal)  |
| y         | int32      | The y-coordinate of the target cell (vertical)    |
| action    | FlagAction | The type of action to perform (DIG or PLACE_FLAG) |

## Mine Result

| Component | Data Type  | Description                                              |
|-----------|------------|----------------------------------------------------------|
| x         | int32      | The x-coordinate of the cell where the action was taken  |
| y         | int32      | The y-coordinate of the cell where the action was taken  |
| action    | FlagAction | The action that was performed                            |
| signal    | MineSignal | The game state after the action (CONTINUE, WIN, or LOSE) |