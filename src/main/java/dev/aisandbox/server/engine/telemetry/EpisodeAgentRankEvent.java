/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
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

  private static final String jsonTemplate = """
      {
          "timestamp":"%s",
          "event":"episode_agent_rank",
          "simulation_name":"%s",
          "session_id":"%s",
          "episode_id":"%s",
          "agent_name":"%s",
          "rank":%d
      }
      """;

  @Override
  public List<String> toJSON() {
    return agentRankList.stream()
        .map(a -> String.format(jsonTemplate, timestamp.toString(), simulationName,
            sessionID, episodeID, a.agentName(), a.rank()))
        .toList();
  }

  @Override
  public void emit(Logger logger) {
    for (AgentRank agent : agentRankList) {
      logger.logRecordBuilder()
          .setBody("Episode Agent Rank")
          .setSeverity(Severity.INFO)
          .setAttribute("simulation_name", simulationName)
          .setAttribute("session_id", sessionID)
          .setAttribute("episode_id", episodeID)
          .setAttribute("agent_name", agent.agentName())
          .setAttribute("rank", String.valueOf(agent.rank()))
          .setTimestamp(timestamp)
          .emit();
    }
  }

  public record AgentRank(String agentName, int rank) {

  }
}