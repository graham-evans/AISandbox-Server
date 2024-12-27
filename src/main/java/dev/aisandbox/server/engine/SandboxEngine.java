package dev.aisandbox.server.engine;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SandboxEngine {

    private final SimulationInfo simulation;
    private final List<Player> players;

}
