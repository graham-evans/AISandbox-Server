package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.output.OutputRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SimulationRunner extends Thread {

    private final Simulation simulation;
    private final OutputRenderer outputRenderer;
    private final List<Player> players;

    private boolean running = true;

    public void stopSimulation() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        outputRenderer.setup();
        log.info("Writing output to {}", outputRenderer.getName());
        log.info("Starting simulation (ctrl-c to exit)...");
        // start simulation
        while (running) {
            simulation.step(outputRenderer);
        }
        // finish simulation
        simulation.close();
        players.forEach(Player::close);
    }

}