/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;

public record SessionStartEvent() implements TelemetryEvent {

  @Override
  public String simulationName() {
    return "";
  }

  @Override
  public String sessionID() {
    return "";
  }

  @Override
  public String episodeID() {
    return "";
  }

  @Override
  public Instant timestamp() {
    return null;
  }
}
