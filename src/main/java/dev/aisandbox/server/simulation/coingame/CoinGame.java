package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CoinGame implements Simulation {

    private final List<Player> players;

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
