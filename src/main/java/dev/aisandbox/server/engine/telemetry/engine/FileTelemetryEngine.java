/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentDoubleScoreEvent.AgentDoubleScore;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentLongScoreEvent.AgentLongScore;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent.AgentRank;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent.AgentResult;
import dev.aisandbox.server.engine.telemetry.event.EpisodeDoubleScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeLongScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeWinEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionFailureEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionStartEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Telemetry engine that writes events as JSON to a session-specific file.
 */
@Slf4j
@RequiredArgsConstructor
public class FileTelemetryEngine implements TelemetryEngine {

  private final File directory;
  private final ObjectMapper mapper = new ObjectMapper();
  private BufferedWriter writer;

  private ObjectNode createCommon(TelemetryEvent event) {
    ObjectNode node = mapper.createObjectNode();
    node.put("@timestamp", event.timestamp().toString());
    node.put("message", event.description());
    node.putObject("ecs").put("version", "8.11");
    node.putObject("service").put("name", "AISandbox");
    ObjectNode eventNode = node.putObject("event");
    eventNode.put("action", event.getClass().getSimpleName());
    eventNode.put("dataset", "aisandbox.episodes");
    eventNode.put("kind", "event");
    eventNode.putArray("category").add("process");
    node.putObject("simulation").put("name", event.simulationName());
    node.putObject("session").put("id", event.sessionID());
    node.putObject("episode").put("id", event.episodeID());
    return node;
  }

  @Override
  public void initialise(String sessionID) {
    File outputFile = new File(directory, sessionID + ".json");
    try {
      writer = new BufferedWriter(new FileWriter(outputFile));
    } catch (IOException e) {
      log.error("Failed to open telemetry file {}", outputFile, e);
    }
  }

  @Override
  public void writeTelemetryEvent(TelemetryEvent event) {
    // check for broken writer
    if (writer == null) {
      return;
    }
    // generate and write JSON objects
    try {
      switch (event) {
        case SessionStartEvent ignored -> {
          ObjectNode root = createCommon(event);
          writer.write(mapper.writeValueAsString(root));
          writer.write("\n");
        }
        case SessionFailureEvent ignored -> {
          ObjectNode root = createCommon(event);
          writer.write(mapper.writeValueAsString(root));
          writer.write("\n");
        }
        case EpisodeWinEvent win -> {
          ObjectNode root = createCommon(event);
          ((ObjectNode) root.get("simulation")).put("win", win.win());
          writer.write(mapper.writeValueAsString(root));
          writer.write("\n");
        }
        case EpisodeDoubleScoreEvent score -> {
          ObjectNode root = createCommon(event);
          ((ObjectNode) root.get("simulation")).put("score", score.score());
          writer.write(mapper.writeValueAsString(root));
          writer.write("\n");
        }
        case EpisodeLongScoreEvent score -> {
          ObjectNode root = createCommon(event);
          ((ObjectNode) root.get("simulation")).put("score", score.score());
          writer.write(mapper.writeValueAsString(root));
          writer.write("\n");
        }
        case EpisodeAgentDoubleScoreEvent agentScores -> {
          for (AgentDoubleScore agentScore : agentScores.agentScoreList()) {
            ObjectNode root = createCommon(event);
            root.put("message", agentScore.description());
            ObjectNode simulation = (ObjectNode) root.get("simulation");
            simulation.putObject("agent").put("name", agentScore.agentName());
            simulation.put("score", agentScore.score());
            writer.write(mapper.writeValueAsString(root));
            writer.write("\n");
          }
        }
        case EpisodeAgentLongScoreEvent agentScores -> {
          for (AgentLongScore agentScore : agentScores.agentScoreList()) {
            ObjectNode root = createCommon(event);
            root.put("message", agentScore.description());
            ObjectNode simulation = (ObjectNode) root.get("simulation");
            simulation.putObject("agent").put("name", agentScore.agentName());
            simulation.put("score", agentScore.score());
            writer.write(mapper.writeValueAsString(root));
            writer.write("\n");
          }
        }
        case EpisodeAgentRankEvent agentRanks -> {
          for (AgentRank agentRank : agentRanks.agentRankList()) {
            ObjectNode root = createCommon(event);
            root.put("message", agentRank.description());
            ObjectNode simulation = (ObjectNode) root.get("simulation");
            simulation.putObject("agent").put("name", agentRank.agentName());
            simulation.put("rank", agentRank.rank());
            writer.write(mapper.writeValueAsString(root));
            writer.write("\n");
          }
        }
        case EpisodeAgentWinLossEvent agentWin -> {
          for (AgentResult agentResult : agentWin.agentResultList()) {
            ObjectNode root = createCommon(event);
            root.put("message", agentResult.description());
            ObjectNode simulation = (ObjectNode) root.get("simulation");
            simulation.putObject("agent").put("name", agentResult.agentName());
            simulation.put("result", agentResult.result().name());
            writer.write(mapper.writeValueAsString(root));
            writer.write("\n");
          }
        }
      }
    } catch (JsonProcessingException e) {
      log.error("Error writing event of type {}",event.getClass().getName(),e);
    } catch (IOException e) {
      log.error("Error writing event to file - stopping writer");
      writer=null;
    }
  }

  @Override
  public void close() {
    if (writer == null) {
      return;
    }
    try {
      writer.close();
    } catch (IOException e) {
      log.error("Failed to close telemetry file", e);
    }
  }

}
