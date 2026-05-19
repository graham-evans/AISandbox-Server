/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import java.time.Instant;
import java.util.List;

/**
 * Telemetry event to denote a simulation episode completing with a win or loss outcome.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 * @param win                 Whether the episode was won
 */
public record EpisodeWinEvent(String simulationName,
                              String sessionID,
                              String episodeID,
                              Instant timestamp,
                              boolean win) implements TelemetryEvent {

  private static final String jsonTemplate = """
      {
          "timestamp":"%s",
          "event":"episode_win",
          "simulation_name":"%s",
          "session_id":"%s",
          "episode_id":"%s",
          "win":%b
      }
      """;

  @Override
  public List<String> toJSON() {
    return List.of(
        String.format(jsonTemplate, timestamp.toString(), simulationName, sessionID,
            episodeID, win));
  }

  @Override
  public void emit(Logger logger) {
    logger.logRecordBuilder()
        .setBody("Episode Win")
        .setSeverity(Severity.INFO)
        .setAttribute("simulation_name", simulationName)
        .setAttribute("session_id", sessionID)
        .setAttribute("episode_id", episodeID)
        .setAttribute("win", String.valueOf(win))
        .setTimestamp(timestamp)
        .emit();
  }
}