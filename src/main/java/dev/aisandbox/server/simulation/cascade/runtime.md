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
      1. Double the multiplier
      2. Call the update logic method to advance
      3. Redraw the screen
10. Build the reward object (calculating the scoreGained from the currentScore-oldScore) and send it to the agent.
11. If there are no moves left, record the episode score and call StartNewEpisode.


## Make Move logic:

Make move takes a board and two x,y pairs (the pair of cells to be swapped) . It will follow the following steps:

1. If the two cells are not next to each other, throw an invalid move exception.
2. If either of the two cells is frozen or a stone, throw an invalid move exception.
3. If both cells are prisms.
4. If one is a prism and the other is a bomb.
5. If one is a prism and the other is a vertical rocket.
6. If one is a prism and the other in a horizontal rocket.
7. Create a copy of the board and swap the selected cells.
8. Check to see if the new board is stable, if it is, throw an invalid move exception.
9. Call update move (once only) to resolve the first set of connections.
10. return the new board


## Update board logic:

This is called on unstable boards to implement gravity and cascades, it takes a board object and follows these steps: