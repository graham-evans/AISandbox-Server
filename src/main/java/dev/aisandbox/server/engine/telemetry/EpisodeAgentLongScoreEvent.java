/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import java.time.Instant;
import java.util.List;

/**
 * Telemetry event to denote a simulation episode completing with a long integer score per agent.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 * @param agentScoreList      The list of agents and their scores
 */
public record EpisodeAgentLongScoreEvent(String simulationName,
                                         String sessionID,
                                         String episodeID,
                                         Instant timestamp,
                                         List<AgentLongScore> agentScoreList) implements
    TelemetryEvent {

  public record AgentLongScore(String agentName, long score) {

    public String description() {
      return "Agent " + agentName + " scored " + score;
    }
  }
}
