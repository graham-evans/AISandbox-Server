/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.aisandbox.server.engine.telemetry.TelemetryEngine;
import dev.aisandbox.server.engine.telemetry.TelemetryEpisodeEvent;
import dev.aisandbox.server.engine.telemetry.TelemetryEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentRankEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeAgentWinLossEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeScoreEvent;
import dev.aisandbox.server.engine.telemetry.event.EpisodeWinEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionFailureEvent;
import dev.aisandbox.server.engine.telemetry.event.SessionStartEvent;
import dev.aisandbox.server.engine.telemetry.event.StepProfileEvent;
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
  private final boolean writeProfile;
  private BufferedWriter writer;

  private ObjectNode createCommon(TelemetryEvent event) {
    ObjectNode node = mapper.createObjectNode();
    // add general entries
    node.put("@timestamp", event.timestamp().toString());
    node.put("message", event.description());
    node.putObject("ecs").put("version", "8.11");
    node.putObject("service").put("name", "AISandbox");
    // add event level entries
    ObjectNode eventNode = node.putObject("event");
    eventNode.put("action", event.eventName());
    eventNode.put("dataset", "aisandbox.episodes");
    eventNode.put("kind", "event");
    eventNode.putArray("category").add("process");
    // add simulation level entries
    ObjectNode simulationNode = eventNode.putObject("simulation");
    simulationNode.put("name", event.simulationName());
    // return common entries
    return node;
  }

  private ObjectNode createCommon(TelemetryEpisodeEvent event) {
    ObjectNode node = createCommon((TelemetryEvent) event);
    // add episode specific lines
    ObjectNode episode = node.putObject("episode");
    episode.put("id", event.episodeId());
    episode.put("number", event.episodeNumber());
    // return common entries
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
    // skip profiling events unless explicitly enabled
    if (event instanceof StepProfileEvent && !writeProfile) {
      return;
    }
    // generate and write JSON objects
    try {
      ObjectNode node = switch (event) {
        case SessionStartEvent startEvent -> createCommon(startEvent);
        case SessionFailureEvent failureEvent -> createCommon(failureEvent);
        case EpisodeWinEvent winEvent ->
            putAtPath(createCommon(winEvent), Boolean.toString(winEvent.win()), "simulation",
                "win");
        case EpisodeScoreEvent scoreEvent ->
            putAtPath(createCommon(scoreEvent), Double.toString(scoreEvent.score()), "simulation",
                "score");
        case EpisodeAgentScoreEvent agentScoreEvent -> JsonBuilder.on(createCommon(agentScoreEvent))
            .put(agentScoreEvent.agentScore(), "simulation", "agent", "score")
            .put(agentScoreEvent.agentName(), "simulation", "agent", "name")
            .build();
        case EpisodeAgentRankEvent agentRankEvent -> JsonBuilder.on(createCommon(agentRankEvent))
            .put(agentRankEvent.agentRank(), "simulation", "agent", "rank")
            .put(agentRankEvent.agentName(), "simulation", "agent", "name")
            .build();
        case EpisodeAgentWinLossEvent agentWinLossEvent ->
            JsonBuilder.on(createCommon(agentWinLossEvent))
                .put(agentWinLossEvent.agentResult().name(), "simulation", "agent", "result")
                .put(agentWinLossEvent.agentName(), "simulation", "agent", "name")
                .build();
        case StepProfileEvent profileEvent -> JsonBuilder.on(createCommon(profileEvent))
            .put(profileEvent.phaseName(), "simulation", "profile", "phase")
            .put(profileEvent.stepNumber(), "simulation", "profile", "step")
            .put(profileEvent.durationMillis(), "simulation", "profile", "duration_ms")
            .put(profileEvent.startTime().toString(), "simulation", "profile", "start")
            .put(profileEvent.stopTime().toString(), "simulation", "profile", "stop")
            .build();
      };
      writer.write(mapper.writeValueAsString(node));
      writer.write("\n");
    } catch (JsonProcessingException e) {
      log.error("Error writing event of type {}", event.getClass().getName(), e);
    } catch (IOException e) {
      log.error("Error writing event to file - stopping writer");
      writer = null;
    }
  }

  public static ObjectNode putAtPath(ObjectNode root, String value, String... keys) {
    ObjectNode current = root;
    for (int i = 0; i < keys.length - 1; i++) {
      JsonNode next = current.get(keys[i]);
      if (next == null || !next.isObject()) {
        next = current.putObject(keys[i]);
      }
      current = (ObjectNode) next;
    }
    current.put(keys[keys.length - 1], value);
    return root;
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
