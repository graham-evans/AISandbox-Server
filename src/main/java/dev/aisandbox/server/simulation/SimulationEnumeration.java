/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.simulation.bandit.BanditScenario;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import dev.aisandbox.server.simulation.maze.MazeBuilder;
import dev.aisandbox.server.simulation.mine.MineHunterScenario;
import dev.aisandbox.server.simulation.twisty.TwistyBuilder;
import lombok.Getter;

/**
 * Enumeration of all available simulations in the AI Sandbox.
 * <p>
 * This enumeration serves as a central registry of all simulation types that can be executed within
 * the AI Sandbox framework. Each enumeration value is associated with a specific
 * {@link SimulationBuilder} that provides the configuration and factory methods for creating
 * instances of that simulation.
 * </p>
 * <p>
 * The available simulations cover a variety of AI problem domains including:
 * </p>
 * <ul>
 *   <li><strong>Reinforcement Learning:</strong> Multi-armed bandits, maze navigation</li>
 *   <li><strong>Game Playing:</strong> Card games, puzzle solving</li>
 *   <li><strong>Search and Optimization:</strong> Pathfinding, constraint satisfaction</li>
 *   <li><strong>Sequential Decision Making:</strong> Resource allocation, strategic planning</li>
 * </ul>
 * <p>
 * Each simulation provides a unique environment for testing and evaluating AI agents,
 * with configurable parameters and real-time visualization capabilities.
 * </p>
 *
 * @see SimulationBuilder
 */
public enum SimulationEnumeration {
  /**
   * The Coin Game simulation - strategic coin removal game.
   * <p>
   * Players take turns removing coins from piles, with the objective being to force the opponent to
   * take the last coin. Features multiple game variants with different pile configurations and move
   * restrictions.
   * </p>
   */
  COIN_GAME(new CoinGameBuilder()),

  /**
   * High-Low Cards game - sequential card prediction.
   * <p>
   * Agent must predict whether the next card in a deck will be higher or lower than the current
   * card. Tests decision-making under uncertainty with observable information about remaining
   * cards.
   * </p>
   */
  HIGH_LOW_CARDS(new HighLowCardsBuilder()),

  /**
   * Multi-armed Bandit problem - exploration vs exploitation.
   * <p>
   * Classic reinforcement learning scenario where an agent must choose between multiple slot
   * machines (bandits) to maximize reward over time. Tests the fundamental explore-exploit tradeoff
   * in sequential decision making.
   * </p>
   */
  MULTI_BANDIT(new BanditScenario()),

  /**
   * Maze navigation - pathfinding and spatial reasoning.
   * <p>
   * Agent must navigate through a maze from start to goal position while dealing with obstacles and
   * potentially limited visibility. Tests search algorithms and spatial planning capabilities.
   * </p>
   */
  MAZE(new MazeBuilder()),

  /**
   * Mine Hunter - logical deduction and risk assessment.
   * <p>
   * Agent must identify hidden mines in a grid using numerical clues, similar to the classic
   * Minesweeper game. Tests logical reasoning and constraint satisfaction under uncertainty.
   * </p>
   */
  MINE(new MineHunterScenario()),

  /**
   * Twisty Puzzles - 3D spatial manipulation.
   * <p>
   * Agent must solve 3D puzzles like Rubik's cubes through sequence of moves. Tests complex state
   * space navigation and goal-oriented planning in high-dimensional spaces.
   * </p>
   */
  TWISTY(new TwistyBuilder());
  /**
   * The {@link SimulationBuilder} instance associated with this simulation type.
   */
  @Getter
  private final SimulationBuilder builder;

  /**
   * Constructs a new instance of the enum, associating it with the given
   * {@link SimulationBuilder}.
   *
   * @param builder the {@link SimulationBuilder} for this simulation type
   */
  SimulationEnumeration(SimulationBuilder builder) {
    this.builder = builder;
  }


}
