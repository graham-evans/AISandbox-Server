package dev.aisandbox.server.simulation.highlowcards;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;

public class HighLowCardsSimulation implements Simulation {

    private final Player player;
    private final int cardCount;

    public HighLowCardsSimulation(Player player,int cardCount) {
        this.player = player;
        this.cardCount = cardCount;
    }

    private void reset() {

    }


    @Override
    public void step() {

    }
}
