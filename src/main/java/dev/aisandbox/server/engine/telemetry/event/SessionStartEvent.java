/*
 *
 *  * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 *  * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 *  * more information.
 *
 */

/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import java.time.Instant;

public record SessionStartEvent(String simulationName,String sessionID,Instant timestamp) implements TelemetryEvent {

  @Override
  public String episodeID() {
    return "";
  }

  @Override
  public String description() {
    return "Simulation "+simulationName()+" started @ "+timestamp.toString();
  }
}
