package dev.aisandbox.server.engine;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SandboxEngine {

    private final SimulationBuilder simulation;
    private final List<NetworkPlayer> players;

}
