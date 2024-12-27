package dev.aisandbox.server;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.SimulationInfo;
import dev.aisandbox.server.simulation.highlowcards.HighLowCards;
import dev.aisandbox.server.simulation.highlowcards.proto.Person;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SandboxServerLauncher implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello World!");
        // create simulation
        SimulationInfo simulationInfo = new HighLowCards();
        // create players
        List<Player> players = new ArrayList<>();
        for (int i=0;i<simulationInfo.getPlayerCount();i++) {
            players.add(new Player());
        }
        // run simulation
        Simulation sim = simulationInfo.createSimulation(players);
        // perform 10 steps
        for (int i=0;i<10;i++) {
            sim.step();
        }
        // create proto
        Person person = Person.newBuilder().setName("name").build();
    }
}
