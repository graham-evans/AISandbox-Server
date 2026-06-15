/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeWinEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionFailureEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionStartEvent;
import java.time.Instant;

public interface TelemetryEvent {

  // define the common fields, so they can be accessed without casting the event.

  String simulationName();

  String sessionID();

  Instant timestamp();

  default String description() {
    return "undefined";
  }

  default String eventName() {
    return getClass().getSimpleName();
  }
}
