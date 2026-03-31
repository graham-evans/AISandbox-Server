# Cascade: A Match-3 AI Simulation

## Goal

**Cascade** is a turn-based match-3 puzzle simulation designed as an AI challenge environment. The goal is straightforward: score as many points as possible within a fixed number of moves by swapping adjacent tiles to form matching groups, triggering chain reactions, and detonating special objects.

Unlike real-time match-3 games, Cascade is fully turn-based — the board only changes when the AI issues a move. This makes it well-suited for planning agents, search-based solvers, and reinforcement learning experiments alike.

# Details

## The Board

The game is played on an **8 × 8 grid** of coloured tiles. At the start of each game, the grid is filled randomly 
with tiles of **five colours**: Red, Blue, Green, Yellow, and Purple. In advanced modes there may also be blocks of 
ice or stones. No starting configuration will contain any pre-existing matches.

### Tiles

Each standard tile has a single colour attribute. Three or more in a straight line (horizontally or vertically) form a valid match. Longer matches and crossing matches trigger bonus scoring and, in some cases, spawn special objects automatically.

In addition to the 'standard' tiles there are several special tiles which can be spawned mid-game by certain matches, or introduced by level configuration. They occupy a single cell and interact with matches and explosions in ways that differ from standard tiles.

### 💣 Bomb

**Spawn condition:** Formed automatically when a straight match of exactly **5 tiles** occurs.

**Appearance:** A dark tile marked with a star or burst symbol, in the colour of the match that created it.

**Behaviour:** When a Bomb is part of a match *or* caught in another explosion, it detonates and removes **all tiles in a 3 × 3 area** centred on itself. Any special objects within the blast radius are also triggered. The Bomb itself counts as a tile removal for scoring.

**AI note:** Bombs are among the highest-value objects on the board. Deliberately setting up a 5-in-a-row to place a Bomb, then engineering a subsequent swap that detonates it, is a core high-scoring strategy.

---

### 🚀 Rocket

**Spawn condition:** Formed automatically when an **L-shaped or T-shaped match** occurs (i.e., two lines share a tile).

**Appearance:** An arrow tile. The direction of the arrow depends on the orientation of the match: a predominantly 
horizontal match produces a horizontal Rocket; a vertical one produces a vertical Rocket; in the colour of the match 
that created it.

**Behaviour:** When triggered, a Rocket fires across its **entire row or column** (depending on its orientation), removing every tile it passes through. Special objects in the path are also triggered.

**AI note:** Rockets are excellent for clearing difficult rows or columns and for triggering chains of other special objects. Two Rockets placed adjacently in opposite orientations create a cross-clearing combo when one is triggered.

---

### 🌈 Prism

**Spawn condition:** Formed automatically when a **match of 6 or more tiles** occurs in a single straight line.

**Appearance:** A shimmering multi-coloured tile with no fixed colour of its own.

**Behaviour:** When swapped with any coloured tile (or triggered by an explosion), the Prism **removes every tile on the board of the matched tile's colour**. If a Prism is swapped with another special object, the effect is amplified — it removes all tiles that share that special object's colour *and* triggers a copy of the special object effect on each removed tile's position.

**AI note:** A Prism is the highest-value single object in the game. When the board contains a large population of one colour, triggering the Prism against that colour can clear 10–15 tiles in a single move, frequently setting off further cascades.

---

### 🧊 Ice Block

**Spawn condition:** Placed by level configuration at game start; not created by player actions.

**Appearance:** A tile encased in a translucent blue shell.

**Behaviour:** An Ice Block traps the tile inside it and doesn't fall with gravity. The trapped tile's colour is 
visible, and can participate in matches, but it cannot be swapped with another tile. To free it, a match must be 
made that **includes** the frozen tile. Bombs and Rockets destroy Ice Blocks instantly.

**AI note:** Ice Blocks restrict the effective colour distribution of the board and block cascade paths. Freeing tiles early is usually worth the investment.

---

### 💀 Stone

**Spawn condition:** Placed by level configuration; also spawned by certain late-game board events.

**Appearance:** A grey, cracked tile.

**Behaviour:** Stones are inert — they do not match, do not fall, and do not count as any colour. They can only be removed by direct explosion from a Bomb or Rocket. Stones block gravity and cascade propagation, splitting the effective board into regions.

**AI note:** Stones are obstacles, not opportunities. Prioritise clearing them with explosives early before they fragment the board into isolated sections that are difficult to chain.

## Turn order

Each turn, the AI selects a **swap** — two horizontally or vertically adjacent tiles — and exchanges their positions.
A swap is only valid if it produces at least one match of three or more tiles or activates a special tile; invalid swaps are rejected and the move is wasted.

After a swap is accepted, the following sequence happens automatically:

1. **Match resolution** — all groups of three or more same-coloured tiles in a row or column are removed (normal 
   tiles) or activated (bombs and rockets) simultaneously.
2. **Special tile activation** — all currently activated bombs and rockets remove their target tiles. If these contain 
   unactivated bombs or rockets then these become activated.
3. **Gravity** — tiles above empty spaces fall downward to fill the gaps.
4. **Refill** — new randomly-coloured tiles drop in from the top to fill any remaining empty columns.
5. **Cascade** — The score multiplier is doubled (starts at 1)

This process then repeats until the board becomes 'stable' - it contains no activated tiles and no rows or columns 
of three coloured tiles.

A standard game lasts **30 moves**. The game ends when the move counter reaches zero.

---

## Scoring

| Event                           | Points                 |
|---------------------------------|------------------------|
| Tile removed (base)             | 10 pts                 |
| Match of 4                      | +50 pts bonus          |
| Match of 5                      | +150 pts bonus         |
| L-shaped or T-shaped match      | +75 pts bonus          |
| Cascade (each subsequent chain) | ×2 multiplier (stacks) |
| Special object destroyed        | See below              |

Cascade multipliers stack multiplicatively. A second level cascade scores 20 points per removed tile, a 
third level scores 40.

---

## Combo Interactions

When two special objects are adjacent and one is triggered, both fire simultaneously. Some notable combos:

| Combo           | Effect                                                                           |
|-----------------|----------------------------------------------------------------------------------|
| Bomb + Bomb     | Combined 5 × 5 area blast                                                        |
| Rocket + Rocket | Clears both its row *and* column (full cross)                                    |
| Bomb + Rocket   | Clears a 3-tile-wide row *and* column through the Bomb's centre                  |
| Prism + Bomb    | Replaces every tile of the target colour with a Bomb, then detonates all of them |
| Prism + Rocket  | Fires a Rocket from the position of every tile of the target colour              |
| Prism + Prism   | Clears the **entire board**                                                      |

---

## Winning and Scoring

The game ends after 30 moves. Your final score is the sum of all points accumulated. There is no "win condition" beyond maximising score — this is an optimisation problem.

A benchmark score table will be published separately. As a rough guide:

| Score           | Rating    |
|-----------------|-----------|
| < 5,000         | Novice    |
| 5,000 – 12,000  | Competent |
| 12,000 – 25,000 | Strong    |
| 25,000 – 50,000 | Expert    |
| > 50,000        | Master    |

---

# Algorithms and Hints

**Model the board state completely**

Your agent needs a complete representation of the board: every cell's content (colour and type), the current move count,
and the current score. Don't discard special-object metadata — a Bomb is fundamentally different from a normal tile of the same colour and must be tracked separately.

**Enumerate legal moves efficiently**

On an 8 × 8 grid there are at most 112 possible swaps (56 horizontal + 56 vertical), but many will be invalid. Pre-filtering to only valid swaps before any evaluation saves significant time. A swap is legal if and only if the resulting board contains at least one new match — this can be checked with a lightweight scan rather than full simulation.

**3. Simulate cascades accurately**

Naive agents that only evaluate the immediate match will badly underestimate the value of moves that set off cascades. Your simulator must correctly implement the full resolution loop: match → gravity → refill → re-check → repeat. Pay particular attention to how special objects chain, especially Bomb-in-blast-radius and Rocket-path interactions.

**Treat special objects as high-value assets**

Special objects typically score 5–20× more than a plain match of three. Early in your development, a strong heuristic is to bias heavily towards any move that triggers or sets up a special object. Later, you can refine this with actual expected-value calculations.

**Think ahead about Prism targeting**

When a Prism is on the board, the colour you pair it with matters enormously. Count the current frequency of each colour on the board before deciding. Triggering a Prism against the minority colour is almost always a mistake; saving it until a colour dominates (or until you can engineer a Prism + special combo) is the stronger play.

**Consider cascade potential, not just immediate matches**

A move that creates a match of three in an isolated corner scores 30 points. A move that creates a modest match but leaves the board in a configuration where gravity creates two further cascades may score 300. Lookahead search (even 1–2 ply) pays dividends here; if full search is too slow, use board-state heuristics like column height variance, colour clustering, or proximity of like-coloured tiles as a proxy.

**Don't ignore Ice and Stone clearance**

It's tempting to evaluate only immediate scoring, but Ice Blocks reduce your effective colour palette and Stones fragment cascade paths. Assign a small positive value to moves that clear these obstacles, even when the direct score is low.

**Balance exploration and exploitation**

In reinforcement learning settings, the action space is narrow enough (at most 112 moves per turn) that exhaustive enumeration is feasible. However, RL agents often collapse onto greedy strategies that miss higher-value long-range setups. Consider reward shaping — giving partial credit for placing specials, setting up chains, or freeing blocked tiles — to encourage richer exploratory behaviour.

**Watch the move budget**

With only 30 moves, every wasted swap on a low-value match is costly. In the late game (fewer than 8 moves remaining), shift weighting towards the highest-variance plays — detonating stored specials, triggering Prisms — rather than safe low-scoring matches.

**Profile your simulator**

The bottleneck for any search-based agent is the speed of board simulation. Implement a compact, copy-efficient board representation (a flat integer array is often fastest), profile the cascade loop, and cache colour counts if you're evaluating Prism moves frequently.

# Protocol

TODO : Detail the protocol used.
