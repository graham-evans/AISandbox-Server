/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import dev.aisandbox.server.engine.network.NetworkAgent;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SandboxEngine {

  private final SimulationBuilder simulation;
  private final List<NetworkAgent> players;

}
