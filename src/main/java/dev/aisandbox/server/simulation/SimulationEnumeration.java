package dev.aisandbox.server.simulation;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.simulation.bandit.BanditScenario;
import dev.aisandbox.server.simulation.coingame.CoinGameBuilder;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import lombok.Getter;

public enum SimulationEnumeration {

    COIN_GAME(new CoinGameBuilder()), HIGH_LOW_CARDS(new HighLowCardsBuilder()), MULTI_BANDIT(new BanditScenario());

    @Getter
    private final SimulationBuilder builder;

    private SimulationEnumeration(SimulationBuilder builder) {
        this.builder = builder;
    }


}
