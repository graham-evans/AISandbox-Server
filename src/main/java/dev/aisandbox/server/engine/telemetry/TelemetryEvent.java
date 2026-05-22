/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;

public sealed interface TelemetryEvent
    permits EpisodeAgentDoubleScoreEvent, EpisodeAgentLongScoreEvent, EpisodeAgentRankEvent,
    EpisodeAgentWinLossEvent, EpisodeDoubleScoreEvent, EpisodeLongScoreEvent, EpisodeWinEvent,
    SessionFailureEvent, SessionStartEvent {

  // define the common fields, so they can be accessed without casting the event.

  String simulationName();

  String sessionID();

  String episodeID();

  Instant timestamp();

  default String description() {
    return "undefined";
  }
}
