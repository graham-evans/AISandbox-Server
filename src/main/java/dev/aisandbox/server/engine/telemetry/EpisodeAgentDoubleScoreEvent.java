package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

public record EpisodeAgentDoubleScoreEvent(String simulationName,
                                           String sessionID,
                                           String episodeID,
                                           Instant episodeFinishedTime,
                                           List<AgentDoubleScore> agentScoreList) implements TelemetryEvent {

    public record AgentDoubleScore(String agentName, double score) {}
}
