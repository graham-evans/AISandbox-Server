# Cascade step logic

The following is a detailed description of some of the more complicated methods of the Cascade simulation.

## Start New Episode logic:
 
Called at the start of the simulation and when a new episode is required.

1. Set the current board to a new (empty CascadeBoard).
2. If the game type is STONE_WORLD, fill the left most column, right most column and bottom row with stone blocks.
3. Call the *initialise* method in the Utils class to fill any empty tiles
4. If the game type is ICE_WORLD, freeze the left most column, right most column and bottom row.

## Step logic:

1. If the board is not valid:
   1. Write a message in the log window saying "Board reshuffled (no valid moves)"
   2. Count the number of bombs on the board.
   3. Count the number of rockets (either version) on the board.
   4. Remove (replace with empty) all tiles that are not stone or ice.
   5. Call the *initialise* method in the Utils class to fill any empty tiles.
   6. Randomly upgrade basic (unfrozen) tiles to bombs and rockets to the original numbers.
   7. Check if the board is now valid, if not go back to iv.
   8. Redraw the screen with the *output* method
2. Convert the current board state to protobuf and sent it to the player
3. Get the action object from the agent.
4. Reset the score multiplier to 1.
5. Record the current score.
6. Subtract one from the remaining moves.
7. Get the new board using the Make Move method.
8. If the Make Move method threw an exception:
   1. Write a message "Invalid move - ignoring."
   2. Redraw the screen.
9. If the Make Move returned a board.
   1. Set the current board the new board.
   2. Redraw the screen
   3. While the current board is unstable:
      1. Call the update logic method to advance (it handles multiplier internally)
      2. Redraw the screen
10. Build the reward object (calculating the scoreGained from the currentScore-oldScore) and send it to the agent.
11. If there are no moves left, record the episode score and call StartNewEpisode.


## Make Move logic:

Make move takes a board and two x,y pairs (the pair of cells to be swapped). It will follow the following steps:

1. If the two cells are not orthogonally adjacent (Manhattan distance ≠ 1), throw an invalid move exception.
2. If either of the two cells is ICE, STONE, or EMPTY, throw an invalid move exception.
3. If both cells are PRISM:
   1. Count all cells that are not EMPTY or STONE (this explicitly includes ICE tiles, standard tiles, bombs, rockets, and both prisms themselves).
   2. Replace all of these with EMPTY cells.
   3. Increase the score by the count multiplied by ten, scaled by the current multiplier.
   4. Return the board.
4. If one cell is a PRISM and the other is a BOMB, ROCKET_H, or ROCKET_V (the "special"):
   1. Note the special tile's type and colour.
   2. Replace the PRISM cell with a new tile of that type and colour, and mark it activated.
   3. Replace the original special cell with an EMPTY cell.
   4. For every STANDARD tile of the noted colour on the board: replace it with a tile of the noted type and colour, and mark it activated.
   5. For every existing BOMB, ROCKET_H, or ROCKET_V tile of that colour already on the board: mark it activated (do not convert — just trigger it).
   6. For each ICE tile of the noted colour: replace it with a STANDARD tile of that colour (unfreeze it). Unfrozen tiles are not subsequently converted to specials.
   7. Increase the score by (count of converted standard tiles + 1) × 10, where the +1 accounts for the prism cell.
   8. Return the board (activated tiles will fire during the update loop).
5. If one cell is a PRISM and the other is a STANDARD tile:
   1. Remember the colour of the standard tile.
   2. Count all STANDARD tiles of this colour on the board (include the partner tile itself; do not count the prism).
   3. Replace the PRISM cell with an EMPTY cell.
   4. Replace each counted STANDARD tile with an EMPTY cell.
   5. For each ICE tile of this colour: replace it with a STANDARD tile of that colour (unfreeze it — it does not score directly here).
   6. Activate any BOMB, ROCKET_H, or ROCKET_V tiles of this colour (they will fire during the update loop).
   7. Increase the score by the count multiplied by ten, scaled by the current multiplier.
   8. Return the board.
6. If both cells are BOMB:
   1. Remove both bombs (replace with EMPTY).
   2. For each non-EMPTY cell within the 5×5 area centred on each bomb's original position (this includes STONE and ICE):
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated (it will fire in the update loop).
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the swapped bomb's colour (same effect as described in Update Priority 2).
      - Otherwise (STANDARD, ICE, STONE): replace with EMPTY.
   3. Increase the score by the count of tiles destroyed (replaced with EMPTY in step 2) multiplied by ten, scaled by the current multiplier.
   4. Return the board.
7. If one cell is a BOMB and the other is a ROCKET_H or ROCKET_V:
   1. Remove both tiles (replace with EMPTY).
   2. Destroy the four diagonal neighbours of the BOMB's original position (clamped to board bounds). For each diagonal cell:
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated.
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the swapped bomb's colour.
      - Otherwise (STANDARD, ICE, STONE): replace with EMPTY.
   3. Fire in all four cardinal directions from the BOMB's original position. In each direction, process cells one at a time:
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated. Continue past it.
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the swapped bomb's colour. Continue.
      - If it is STONE: replace with EMPTY (destroyed). **Stop in this direction.**
      - If it is ICE: replace with EMPTY (destroyed). Continue.
      - Otherwise: replace with EMPTY. Continue.
   4. Increase the score by the count of tiles destroyed (replaced with EMPTY in steps 2 and 3) multiplied by ten, scaled by the current multiplier.
   5. Return the board.
8. If both cells are ROCKET (any combination of ROCKET_H and ROCKET_V):
   1. Remove both rockets (replace with EMPTY).
   2. Each rocket fires along its row and column from its original position. In each direction, process cells one at a time:
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated. Continue past it.
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the swapped rocket's colour. Continue.
      - If it is STONE: replace with EMPTY (destroyed). **Stop in this direction.**
      - If it is ICE: replace with EMPTY (destroyed). Continue.
      - Otherwise: replace with EMPTY. Continue.
   3. Increase the score by the count of tiles destroyed multiplied by ten, scaled by the current multiplier.
   4. Return the board.
9. (Normal swap) Create a copy of the board and swap the two selected cells.
10. Check whether the swap created at least one run of three or more matching tiles at either swapped position. If no run was created, throw an invalid move exception.
11. Return the new board (the update loop will resolve all matches).


## Update board logic:

This is called on unstable boards to advance the board state by one step. It is called repeatedly until the board is stable. Each call performs the single highest-priority applicable action and returns, so the caller can redraw the screen between steps.

Note: the score multiplier is managed exclusively in Priority 1 (gravity and refill). It doubles each time gravity or refill makes changes to the board, ensuring the multiplier increases between scoring waves without needing to be managed in each scoring path.

### Priority 1 — Gravity and refill

1. For each column, within each segment bounded by STONE and ICE tiles:
   a. Compact all fallable tiles downward so that empty cells bubble to the top of the segment.
2. Fill any empty cells at the top of open column segments (not sealed above by STONE or ICE) with new random STANDARD tiles.
3. If any tiles moved or were created, double the score multiplier and return. (No scoring occurs in this step.)

### Priority 2 — Resolve activated specials

If any tiles on the board have their activated flag set:

1. Collect all currently activated tiles into a processing set and clear their activated flags.
2. For each activated BOMB in the set:
   a. Replace the bomb cell with EMPTY.
   b. For each cell in the 3×3 area centred on the bomb (clamped to board bounds):
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated (chain reaction).
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the bomb's colour (see below).
      - If it is STONE: replace with EMPTY (bombs destroy stone).
      - If it is ICE: replace with EMPTY (destroyed, not unfrozen).
      - Otherwise: replace with EMPTY.
   c. Add tiles destroyed × TILE_SCORE × current multiplier to the score.
3. For each activated ROCKET_H in the set:
   a. Replace the rocket cell with EMPTY.
   b. Fire in both directions along the row from the rocket's position. In each direction, process cells one at a time:
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated (chain reaction). Continue past it.
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the rocket's colour (see below). Continue.
      - If it is STONE: replace with EMPTY (destroyed). **Stop in this direction.**
      - If it is ICE: replace with EMPTY (destroyed). Continue.
      - Otherwise: replace with EMPTY. Continue.
   c. Add tiles destroyed × TILE_SCORE × current multiplier to the score.
4. For each activated ROCKET_V in the set:
   a. Replace the rocket cell with EMPTY.
   b. Fire in both directions along the column from the rocket's position. In each direction, process cells one at a time:
      - Skip already-EMPTY cells.
      - If it is a BOMB, ROCKET_H, or ROCKET_V: mark it activated (chain reaction). Continue past it.
      - If it is a PRISM: replace with EMPTY and trigger the prism effect using the rocket's colour (see below). Continue.
      - If it is STONE: replace with EMPTY (destroyed). **Stop in this direction.**
      - If it is ICE: replace with EMPTY (destroyed). Continue.
      - Otherwise: replace with EMPTY. Continue.
   c. Add tiles destroyed × TILE_SCORE × current multiplier to the score.
5. If any new tiles were marked activated during steps 2–4, repeat from step 1 of this priority (process the full chain reaction within the same call).
6. Return.

**Prism triggered by explosion:** When a bomb or rocket explosion hits a PRISM, the prism fires using the colour of the bomb or rocket that destroyed it. The effect is:
   - Replace the PRISM with EMPTY.
   - Replace all STANDARD tiles of that colour with EMPTY.
   - Mark all BOMB, ROCKET_H, or ROCKET_V tiles of that colour as activated (chain reaction — they will be processed in the next pass of step 5).
   - Replace all ICE tiles of that colour with STANDARD tiles of that colour (unfreeze them).
   - Add the count of STANDARD tiles destroyed × TILE_SCORE × current multiplier to the score.

### Priority 3 — Resolve matches and spawn specials

1. Scan the board for all horizontal and vertical runs of 3 or more matchable tiles of the same colour. Mark every tile belonging to at least one run.
2. If no tiles are marked, the board is stable — return without changes.
3. Determine special spawns from the match geometry. For each distinct match group, evaluate the shape and spawn at most one special:
   a. **Straight run of exactly 4**: no special is spawned. The four tiles are removed normally for points.
   b. **Straight run of exactly 5**: spawn a BOMB at the centre of the run.
   c. **Straight run of 6 or more**: spawn a PRISM at the centre of the run.
   d. **L-shape or T-shape** (a cell at the intersection of a horizontal and vertical run, each of length 3+): spawn a ROCKET at the intersection (ROCKET_H if the horizontal arm is longer or equal, ROCKET_V if the vertical arm is longer).
   e. If a cell qualifies for multiple spawn rules, use the highest tier: PRISM > BOMB > ROCKET.
   f. Spawned specials inherit the colour of the matched run and are placed **unactivated**.
4. Process the marked tiles:
   a. If a marked tile is a BOMB, ROCKET_H, or ROCKET_V: mark it activated (it will fire on the next update call via Priority 2). Do **not** remove it.
   b. Otherwise: replace it with EMPTY — unless it is the designated position for a spawned special, in which case place the new special there instead.
5. For each ICE tile that is orthogonally adjacent to at least one tile that was removed in step 4b: replace it with a STANDARD tile of its colour (unfreeze it). Unfrozen tiles do not count toward scoring.
6. Add tiles removed × TILE_SCORE × current multiplier to the score. Count only tiles replaced with EMPTY in step 4b — do not count activated specials or unfrozen ice.
7. Return.