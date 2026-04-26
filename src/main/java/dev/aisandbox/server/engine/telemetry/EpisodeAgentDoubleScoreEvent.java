/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import io.opentelemetry.api.logs.Logger;

import java.time.Instant;
import java.util.List;

public record EpisodeAgentDoubleScoreEvent(String simulationName,
                                           String sessionID,
                                           String episodeID,
                                           Instant episodeFinishedTime,
                                           List<AgentDoubleScore> agentScoreList) implements TelemetryEvent {

    public record AgentDoubleScore(String agentName, double score) {}

    private static final String jsonTemplate = """
        {
            "timestamp":"%s",
            "event":"episode_agent_double_score",
            "simulation_name":"%s",
            "session_id":"%s",
            "episode_id":"%s",
            "agent_name":"%s",
            "agent_score":"%d"
        }
        """;

    @Override
    public List<String> toJSON() {
        return List.of();
    }

    @Override
    public void emit(Logger logger) {

    }
}
