# Test Scenarios: `isStable` and `isValid` in CascadeBoardUtils

## `isStable`

### Should return `true` (board IS stable)

1. Empty board — no tiles, nothing to match
2. Full board of mixed colours with no runs of 3
3. Run of exactly 2 same-colour tiles in a row (one short of a match)
4. Stones breaking what would otherwise be a run of 3
5. Empty cells breaking what would otherwise be a run of 3
6. Prism sitting between two same-colour tiles (prism is not matchable, breaks the run)
7. Board containing only stones

### Should return `false` (board is NOT stable)

1. Horizontal run of exactly 3 same-colour standard tiles
2. Horizontal run of 4+ same-colour standard tiles
4. Vertical run of exactly 3 same-colour standard tiles
5. Vertical run of 4+ same-colour standard tiles
6. Run of 3 at the far edge of the board (columns 5–7 or rows 5–7)
7. Run of 3 made up of mixed matchable types: e.g. standard + bomb + rocket of same colour
8. Bomb of same colour as 2 adjacent standard tiles forming a run of 3
9. Ice tile of same colour as 2 adjacent standard tiles forming a run of 3

---

## `isValid`

### Should return `true` (there IS a valid move)

1. Board containing a PRISM next to any non frozen, non stone tile
2. Board containing two PRISMs next to each other.
3. One swap away from a horizontal match of 3 (e.g. `R B R R` — swap positions 0 and 1)
4. One swap away from a vertical match of 3
5. One swap away from a match of three containing a bomb.
6. One swap away from a match of three containing a rocket.
7. One swap away from a match of three with only bombs and rockets.
8. Valid swap exists at the far edge of the board
9. Valid swap involves a standard tile adjacent to a bomb of the same colour (swap creates a match)
10. Board where there is only one valid swap (all other swaps are invalid)

### Should return `false` (there is NO valid move)

1. Empty board — no tiles to swap
2. Board of all stones — no swappable tiles
3. Board of all ice — no swappable tiles (ice is excluded from swaps)
4. Board where all swappable tiles have different colours from all their neighbours (no swap produces a match), and no PRISM objects present
5. Board where adjacent same-colour pairs exist, but swapping them doesn't produce a run of 3
6. Only valid-looking pair is separated by a stone or ice (so the swap can't happen)
7. PRISM completely surrounded by stones — it is present but has no swappable neighbour to pair with *(current implementation returns true — BUG)*
8. BOMB present but no swap on the board creates a match *(current implementation returns true — BUG)*
9. ROCKET present but no swap on the board creates a match *(current implementation returns true — BUG)*
