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
 * Telemetry event to denote a single agent failing to complete a task.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 */
public record EpisodeFailureEvent(String simulationName,
                                  String sessionID,
                                  String episodeID,
                                  Instant timestamp) implements TelemetryEvent {

  private static final String jsonTemplate = """
      {
          "timestamp":"%s",
          "event":"episode_failure",
          "simulation_name":"%s",
          "session_id":"%s",
          "episode_id":"%s"
      }
      """;


  @Override
  public List<String> toJSON() {
    return List.of(
        String.format(jsonTemplate, timestamp.toString(), simulationName, sessionID,
            episodeID));
  }

  @Override
  public void emit(Logger logger) {
    logger.logRecordBuilder()
        .setBody("Episode Failure")
        .setSeverity(Severity.INFO)
        .setAttribute("simulation_name", simulationName)
        .setAttribute("session_id", sessionID)
        .setAttribute("episode_id", episodeID)
        .setTimestamp(timestamp)
        .emit();
  }
}
