/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEpisodeEvent;

import java.time.Instant;

/**
 * Telemetry event to denote a simulation episode completing with a rank per agent.
 *
 * @param simulationName The name of the simulation
 * @param sessionId      The session identifier
 * @param episodeId      The episode identifier
 * @param episodeNumber  The number of the episode
 * @param timestamp      The time the event was created
 * @param agentName      The name of the agent being reported
 * @param agentRank      The rank (1st being highest) of the agent
 */
public record EpisodeAgentRankEvent(String simulationName,
                                    String sessionId,
                                    String episodeId,
                                    int episodeNumber,
                                    Instant timestamp,
                                    String agentName,
                                    int agentRank) implements TelemetryEpisodeEvent {

    public String description() {
        return "Agent " + agentName + " ranked " + agentRank;
    }
}
