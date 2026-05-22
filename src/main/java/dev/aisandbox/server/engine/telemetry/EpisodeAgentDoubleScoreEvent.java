/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

/**
 * Telemetry event to denote a simulation episode completing with a double precision score per
 * agent.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 * @param agentScoreList      The list of agents and their scores
 */
public record EpisodeAgentDoubleScoreEvent(String simulationName,
                                           String sessionID,
                                           String episodeID,
                                           Instant timestamp,
                                           List<AgentDoubleScore> agentScoreList) implements
    TelemetryEvent {

  public record AgentDoubleScore(String agentName, double score) {

  }
}
