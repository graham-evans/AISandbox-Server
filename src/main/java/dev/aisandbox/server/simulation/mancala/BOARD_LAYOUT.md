# Mancala Board Layout

## Overview

The Mancala simulation implements the **Kalah** variant. The board consists of **12 pits** (6 per player) and **2 stores** (one per player), stored internally as a single `int[14]` array.

## Internal Array Layout

The board state is held in `MancalaBoard.board`, an `int[14]` where each element is a seed count:

```
Index:   0   1   2   3   4   5   6   7   8   9  10  11  12  13
       |---Player 1 pits---|  P1  |---Player 2 pits---|  P2
                             store                      store
```

| Index | Role |
|-------|------|
| 0-5 | Player 1's pits (pit 0 through pit 5) |
| 6 | Player 1's store (mancala) |
| 7-12 | Player 2's pits (pit 0 through pit 5) |
| 13 | Player 2's store (mancala) |

Player 2's pit 0 is at index 7, pit 1 at index 8, and so on up to pit 5 at index 12. The conversion from player-relative pit number to absolute index is:

- **Player 1**: `pitIndex = pit` (i.e. indices 0-5)
- **Player 2**: `pitIndex = pit + 7` (i.e. indices 7-12)

## Physical Board Mapping

On the rendered board (and in a real Mancala game), the two rows of pits face each other, with stores at either end. Player 2's pits are on top, displayed right-to-left (pit 5 on the left, pit 0 on the right). Player 1's pits are on the bottom, displayed left-to-right.

```
         Player 2's side (top row, displayed right to left)
      +----+----+----+----+----+----+----+----+
      | P2 |  5 |  4 |  3 |  2 |  1 |  0 | P1 |
      |store| 12 | 11 | 10 |  9 |  8 |  7 |store|
      |[13]|    |    |    |    |    |    | [6] |
      +----+----+----+----+----+----+----+----+
      |    |  0 |  1 |  2 |  3 |  4 |  5 |    |
      |    |  0 |  1 |  2 |  3 |  4 |  5 |    |
      +----+----+----+----+----+----+----+----+
         Player 1's side (bottom row, displayed left to right)

  Top number = player-relative pit index
  Bottom number = absolute array index
```

This layout means pits directly across from each other are **opposite pits**. The opposite of any pit at index `i` is at index `12 - i`:

| Player 1 pit | Array index | Opposite index | Player 2 pit |
|:---:|:---:|:---:|:---:|
| 0 | 0 | 12 | 5 |
| 1 | 1 | 11 | 4 |
| 2 | 2 | 10 | 3 |
| 3 | 3 | 9 | 2 |
| 4 | 4 | 8 | 1 |
| 5 | 5 | 7 | 0 |

## Initial State

Each pit starts with a configurable number of seeds (3, 4, 5, or 6 — default is 4). Both stores start at 0. With 4 seeds per pit, the initial array is:

```
[4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0]
```

## Sowing Mechanic

Seeds are sown **counter-clockwise** by incrementing the array index (mod 14):

1. Pick up all seeds from the chosen pit (set it to 0).
2. Moving forward through the array one index at a time, drop one seed in each position.
3. **Skip the opponent's store** — if the next index would be the opponent's store, advance one more.

For Player 1, sowing skips index 13 (Player 2's store).
For Player 2, sowing skips index 6 (Player 1's store).

## Special Rules

### Extra Turn
If the last seed lands in the current player's own store, that player gets another turn.

### Capture
If the last seed lands in an **empty pit on the current player's own side**, and the **opposite pit** has seeds, the player captures both the landing seed and all seeds in the opposite pit. All captured seeds go into the player's store.

### Game Over
The game ends when **either** player has all six of their pits empty. The other player then collects all seeds remaining on their side into their own store. The player with more seeds in their store wins.

## Relative Board (`getRelativeBoard`)

The `MancalaBoard.getRelativeBoard(int player)` method returns a 14-element array rotated so that the requesting player's data always comes first, regardless of whether they are player 0 or player 1:

```
Index:   0   1   2   3   4   5   6   7   8   9  10  11  12  13
       |----Your pits------| Your |--Opponent's pits--| Opp.
                             store                      store
```

| Index | Meaning |
|-------|---------|
| 0-5 | Your pits (pit 0 through pit 5) |
| 6 | Your store |
| 7-12 | Opponent's pits (pit 0 through pit 5) |
| 13 | Opponent's store |

For player 0, the relative board is identical to the internal array. For player 1, the array is rotated by 7 positions so their pits (internal indices 7-12) appear at indices 0-5.

This means an agent never needs to know its player number — it always sees its own pits at indices 0-5 and can always sow from pit indices 0-5.

## Protobuf State Representation

The state sent to agents via protobuf (`MancalaState`) uses the relative board:

- `board` (repeated int32, length 14): the full board from the current player's perspective, as returned by `getRelativeBoard`. Indices 0-5 are always the current player's pits, index 6 is their store, indices 7-12 are the opponent's pits, and index 13 is the opponent's store.
- `validMoves`: list of pit indices (0-5) the current player can sow from (non-empty pits).

The agent responds with `MancalaAction.selectedPit` (0-5), which indexes directly into the `board[0-5]` range.
