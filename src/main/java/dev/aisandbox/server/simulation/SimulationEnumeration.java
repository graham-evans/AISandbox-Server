package dev.aisandbox.server.simulation;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.simulation.bandit.BanditScenario;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import dev.aisandbox.server.simulation.maze.MazeBuilder;
import lombok.Getter;

/**
 * Enum class representing different types of simulations that can be run.
 * <p>
 * Each simulation type is associated with a {@link SimulationBuilder} instance,
 * which provides the necessary configuration and setup for running the simulation.
 */
public enum SimulationEnumeration {
    /**
     * The Coin Game simulation, where a player must collect coins in a grid while avoiding obstacles.
     */
    COIN_GAME(new CoinGameBuilder()),
    /**
     * The High-Low Cards game, where a player tries to guess the value of a card.
     */
    HIGH_LOW_CARDS(new HighLowCardsBuilder()),
    /**
     * A variation of the Bandit problem, where a player must make decisions based on rewards and penalties.
     */
    MULTI_BANDIT(new BanditScenario()),
    /**
     * The Maze simulation, where a player must navigate through a grid to reach the goal.
     */
    MAZE(new MazeBuilder());
    /**
     * The {@link SimulationBuilder} instance associated with this simulation type.
     */
    @Getter
    private final SimulationBuilder builder;

    /**
     * Constructs a new instance of the enum, associating it with the given {@link SimulationBuilder}.
     *
     * @param builder the {@link SimulationBuilder} for this simulation type
     */
    private SimulationEnumeration(SimulationBuilder builder) {
        this.builder = builder;
    }


}
