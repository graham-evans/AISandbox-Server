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
 * Telemetry event to denote a simulation episode completing with a double precision score per
 * agent.
 *
 * @param simulationName      The name of the simulation
 * @param sessionID           The session identifier
 * @param episodeID           The episode identifier
 * @param episodeFinishedTime The time the event was created
 * @param agentScoreList      The list of agents and their scores
 */
public record EpisodeAgentDoubleScoreEvent(String simulationName,
                                           String sessionID,
                                           String episodeID,
                                           Instant episodeFinishedTime,
                                           List<AgentDoubleScore> agentScoreList) implements
    TelemetryEvent {

  private static final String jsonTemplate = """
      {
          "timestamp":"%s",
          "event":"episode_agent_double_score",
          "simulation_name":"%s",
          "session_id":"%s",
          "episode_id":"%s",
          "agent_name":"%s",
          "score":%f
      }
      """;

  @Override
  public List<String> toJSON() {
    return agentScoreList.stream()
        .map(a -> String.format(jsonTemplate, episodeFinishedTime.toString(), simulationName,
            sessionID, episodeID, a.agentName(), a.score()))
        .toList();
  }

  @Override
  public void emit(Logger logger) {
    for (AgentDoubleScore agent : agentScoreList) {
      logger.logRecordBuilder()
          .setBody("Episode Agent Double Score")
          .setSeverity(Severity.INFO)
          .setAttribute("simulation_name", simulationName)
          .setAttribute("session_id", sessionID)
          .setAttribute("episode_id", episodeID)
          .setAttribute("agent_name", agent.agentName())
          .setAttribute("score", String.valueOf(agent.score()))
          .setTimestamp(episodeFinishedTime)
          .emit();
    }
  }

  public record AgentDoubleScore(String agentName, double score) {

  }
}