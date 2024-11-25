package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;

@Slf4j
public class CoinGame implements Simulation {

    private final List<Player> players;
    private final CoinScenario scenario;
    private final Theme theme;
    private int[] coins;

    public CoinGame(final List<Player> players, final CoinScenario scenario, final Theme theme) {
        this.players = players;
        this.scenario = scenario;
        this.theme = theme;
        coins = new int[scenario.getRows().length];
    }

    private void reset() {
        // reset the number of coins in each pile
        System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    }

    @Override
    public void step(OutputRenderer output) {
        log.info("Simulation step");

    }

    @Override
    public void close() {

    }

    @Override
    public void visualise(Graphics2D graphics2D) {

    }
}
