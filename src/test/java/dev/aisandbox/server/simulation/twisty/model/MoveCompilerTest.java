/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class MoveCompilerTest {

  @Test
  @DisplayName("Check warning on empty move")
  public void blankMoveTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      TwistyPuzzle puzzle = new TwistyPuzzle();
      Cell c1 = new Cell();
      Cell c2 = new Cell();
      Cell c3 = new Cell();
      puzzle.getCells().add(c1);
      puzzle.getCells().add(c2);
      puzzle.getCells().add(c3);
      Move m1 = new Move();
      m1.setName("Null Move");
      // compile moves should return a warning as there is an empty move
      puzzle.addMove(m1);
    });
  }

  @Test
  @DisplayName("Duplicate move name detection")
  public void duplicateMoveTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      TwistyPuzzle puzzle = new TwistyPuzzle();
      Cell c1 = new Cell();
      Cell c2 = new Cell();
      Cell c3 = new Cell();
      puzzle.getCells().add(c1);
      puzzle.getCells().add(c2);
      puzzle.getCells().add(c3);
      // add move
      Move m1 = new Move();
      m1.setName("Move1");
      MoveLoop loop1 = new MoveLoop();
      loop1.getCells().add(c2);
      loop1.getCells().add(c3);
      m1.getLoops().add(loop1);
      puzzle.addMove(m1);
      // add duplicate move
      Move m2 = new Move();
      m2.setName("Move1");
      MoveLoop loop2 = new MoveLoop();
      loop2.getCells().add(c2);
      loop2.getCells().add(c3);
      m2.getLoops().add(loop2);
      puzzle.addMove(m2);
      // compile moves should return an exception as there is already a move with this name
    });
  }

  @Test
  @DisplayName("Make move test 1")
  public void move1Test() {
    TwistyPuzzle puzzle = new TwistyPuzzle();
    // make 5 cells
    Cell c1 = new Cell();
    Cell c2 = new Cell();
    Cell c3 = new Cell();
    Cell c4 = new Cell();
    Cell c5 = new Cell();
    // add them to the puzzle
    puzzle.getCells().add(c1);
    puzzle.getCells().add(c2);
    puzzle.getCells().add(c3);
    puzzle.getCells().add(c4);
    puzzle.getCells().add(c5);
    // Create a move 2->3->4->2
    Move m1 = new Move();
    m1.setName("Move1");
    MoveLoop loop1 = new MoveLoop();
    loop1.getCells().add(c2);
    loop1.getCells().add(c3);
    loop1.getCells().add(c4);
    m1.getLoops().add(loop1);
    puzzle.addMove(m1);
    // perform the move on a state
    String state = "ABCDE";
    String state2 = puzzle.getCompiledMoves().get("Move1").applyMove(state);
    assertEquals("ADBCE", state2);
  }

  @Test
  @DisplayName("Make move test 2")
  public void move2Test() {
    TwistyPuzzle puzzle = new TwistyPuzzle();
    // make 5 cells
    Cell c1 = new Cell();
    Cell c2 = new Cell();
    Cell c3 = new Cell();
    Cell c4 = new Cell();
    Cell c5 = new Cell();
    // add them to the puzzle
    puzzle.getCells().add(c1);
    puzzle.getCells().add(c2);
    puzzle.getCells().add(c3);
    puzzle.getCells().add(c4);
    puzzle.getCells().add(c5);
    // Create a move 1->2->3->4->5->1
    Move m1 = new Move();
    m1.setName("Move1");
    MoveLoop loop1 = new MoveLoop();
    loop1.getCells().add(c1);
    loop1.getCells().add(c2);
    loop1.getCells().add(c3);
    loop1.getCells().add(c4);
    loop1.getCells().add(c5);
    m1.getLoops().add(loop1);
    puzzle.addMove(m1);
    // perform the move on a state
    String state = "ABCDE";
    String state2 = puzzle.getCompiledMoves().get("Move1").applyMove(state);
    assertEquals("EABCD", state2);
  }

}
