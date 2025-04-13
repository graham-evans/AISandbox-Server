package dev.aisandbox.server.engine;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SandboxEngine {

  private final SimulationBuilder simulation;
  private final List<NetworkAgent> players;

}
