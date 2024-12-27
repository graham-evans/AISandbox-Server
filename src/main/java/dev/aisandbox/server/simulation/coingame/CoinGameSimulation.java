package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.simulation.coingame.proto.ServerHandshake;
import dev.aisandbox.server.simulation.coingame.proto.ServerHandshakeOrBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CoinGameSimulation implements Simulation {

    private final List<Player> players;

    @Override
    public void step() {
        log.info("Simulation step");

    }
}
