package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

public record EpisodeAgentLongScoreEvent(String simulationName,
                                         String sessionID,
                                         String episodeID,
                                         Instant episodeFinishedTime,
                                         List<AgentLongScore> agentScoreList) implements TelemetryEvent {

    public record AgentLongScore(String agentName, long score) {}

}
