/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

public record EpisodeAgentWinLossEvent(String simulationName,
                                       String sessionID,
                                       String episodeID,
                                       Instant episodeFinishedTime,
                                       List<AgentResult> agentResultList) implements TelemetryEvent {

    public record AgentResult(String agentName,Result result) {}

    public enum Result {
        WIN,
        LOSE,
        DRAW
    }
}
