package dev.aisandbox.server;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.simulation.highlowcards.HighLowCardsBuilder;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SandboxServerLauncher implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World!");
        // create simulation
        SimulationBuilder simulationBuilder = new HighLowCardsBuilder();
        // create players
        List<Player> players = List.of(new Player("Player",9000, ClientAction.class));
        // create simulation
        Simulation sim = simulationBuilder.build(players);
        // create output
        OutputRenderer out = new NullOutputRenderer();
        // perform 10 steps
        for (int i = 0; i < 10; i++) {
            sim.step(out);
        }
        // finish simulation
        sim.close();
        players.forEach(Player::close);
    }
}
