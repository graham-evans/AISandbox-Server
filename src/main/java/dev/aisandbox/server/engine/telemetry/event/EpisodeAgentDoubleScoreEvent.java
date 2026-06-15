/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEpisodeEvent;

import java.time.Instant;

/**
 * Telemetry event to denote a simulation episode completing with a double precision score per
 * agent.
 *
 * @param simulationName The name of the simulation
 * @param sessionId      The session identifier
 * @param episodeId      The episode identifier
 * @param episodeNumber  The number of the episode
 * @param timestamp      The time the event was created
 * @param agentName      The agent being reported on
 * @param agentScore     The score of the agent at the end of the episode
 *
 */
public record EpisodeAgentDoubleScoreEvent(String simulationName,
                                           String sessionId,
                                           String episodeId,
                                           int episodeNumber,
                                           Instant timestamp,
                                           String agentName,
                                           double agentScore) implements TelemetryEpisodeEvent {

    public String description() {
        return "Agent " + agentName + " scored " + agentScore;
    }

}
