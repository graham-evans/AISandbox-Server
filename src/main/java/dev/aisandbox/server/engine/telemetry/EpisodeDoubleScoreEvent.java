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
 * Telemetry event to denote a simulation episode completing with a double precision score.
 *
 * @param simulationName The name of the simulation
 * @param sessionID The session identifier
 * @param episodeID The episode identifier
 * @param episodeFinishedTime The time the event was created
 * @param score The score for the episode
 */
public record EpisodeDoubleScoreEvent(String simulationName,
                                      String sessionID,
                                      String episodeID,
                                      Instant episodeFinishedTime,
                                      double score) implements TelemetryEvent {
    private static final String jsonTemplate = """
        {
            "timestamp":"%s",
            "event":"episode_double_score",
            "simulation_name":"%s",
            "session_id":"%s",
            "episode_id":"%s",
            "score":%f
        }
        """;

    @Override
    public List<String> toJSON() {
        return List.of(String.format(jsonTemplate, episodeFinishedTime.toString(), simulationName, sessionID, episodeID, score));
    }

    @Override
    public void emit(Logger logger) {
        logger.logRecordBuilder()
                .setBody("Episode Double Score")
                .setSeverity(Severity.INFO)
                .setAttribute("simulation_name", simulationName)
                .setAttribute("session_id", sessionID)
                .setAttribute("episode_id", episodeID)
                .setAttribute("score", String.valueOf(score))
                .setTimestamp(episodeFinishedTime)
                .emit();
    }
}