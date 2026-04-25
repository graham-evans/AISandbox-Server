package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

public record EpisodeAgentRankEvent(String simulationName,
                                    String sessionID,
                                    String episodeID,
                                    Instant episodeFinishedTime,
                                    List<AgentRank> agentRankList) implements TelemetryEvent {

    public record AgentRank(String agentName, int rank) {}

}
