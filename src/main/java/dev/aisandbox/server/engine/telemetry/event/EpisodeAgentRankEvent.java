/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.event;

import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import java.time.Instant;
import java.util.List;

/**
 * Telemetry event to denote a simulation episode completing with a rank per agent.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param timestamp The time the event was created
 * @param agentRankList       The list of agents and their ranks
 */
public record EpisodeAgentRankEvent(String simulationName,
                                    String sessionID,
                                    String episodeID,
                                    Instant timestamp,
                                    List<AgentRank> agentRankList) implements TelemetryEvent {

  public record AgentRank(String agentName, int rank) {

    public String description() {
      return "Agent " + agentName + " ranked " + rank;
    }
  }
}
