/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.engine;

import dev.aisandbox.server.engine.SimulationVersion;
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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * Telemetry engine that forwards events to an OpenTelemetry collector via OTLP/HTTP.
 */
@Slf4j
public class OtelTelemetryEngine implements TelemetryEngine {

  private final OtlpHttpLogRecordExporter exporter;
  private final OpenTelemetrySdk openTelemetry;
  private final SdkLoggerProvider loggerProvider;
  private final Logger logger;
  private final boolean writeProfile;

  public OtelTelemetryEngine(String collectorUrl, boolean writeProfile) {
    log.info("Setting up connection to {}", collectorUrl);
    exporter = OtlpHttpLogRecordExporter.builder()
        .setEndpoint(collectorUrl)
        // .addHeader("Authorization", "Bearer <token>")
        .build();

    Resource resource = Resource.getDefault().merge(
        Resource.create(Attributes.builder()
            .put("service.name", "AISandbox")
            .put("service.version", SimulationVersion.get())
            .build()));

    loggerProvider = SdkLoggerProvider.builder()
        .setResource(resource)
        .addLogRecordProcessor(BatchLogRecordProcessor.builder(exporter).build())
        .build();

    openTelemetry = OpenTelemetrySdk.builder()
        .setLoggerProvider(loggerProvider)
        .build();

    logger = openTelemetry.getLogsBridge().loggerBuilder("aisandbox.telemetry")
        .setInstrumentationVersion(SimulationVersion.get())
        .build();
    this.writeProfile = writeProfile;
  }

  private LogRecordBuilder createCommon(TelemetryEvent event) {
    return logger.logRecordBuilder()
        .setTimestamp(event.timestamp())
        .setBody(event.description())
        .setSeverity(Severity.INFO)
        .setSeverityText("INFO")
        .setAttribute("simulation.name", event.simulationName())
        .setAttribute("simulation.session.id", event.sessionId())
        .setEventName(event.eventName())
        .setBody(event.description());
  }

  private LogRecordBuilder createCommon(TelemetryEpisodeEvent event) {
    return createCommon((TelemetryEvent) event)
        .setAttribute("simulation.episode.id", event.episodeId())
        .setAttribute("simulation.episode.number", event.episodeNumber());
  }


  @Override
  public void initialise(String sessionID) {
  }

  @Override
  public void writeTelemetryEvent(TelemetryEvent event) {
    // skip profiling events unless explicitly enabled
    if (event instanceof StepProfileEvent && !writeProfile) {
      return;
    }
    switch (event) {
      case SessionStartEvent startEvent -> createCommon(startEvent).emit();
      case SessionFailureEvent failureEvent -> createCommon(failureEvent)
          .setSeverity(Severity.ERROR)
          .setSeverityText("ERROR")
          .emit();
      case EpisodeWinEvent winEvent -> createCommon(winEvent)
          .setAttribute("simulation.win", winEvent.win())
          .emit();
      case EpisodeScoreEvent scoreEvent -> createCommon(scoreEvent)
          .setAttribute("simulation.score", scoreEvent.score())
          .emit();
      case EpisodeAgentScoreEvent agentScoreEvent -> createCommon(agentScoreEvent)
          .setAttribute("simulation.agent.name", agentScoreEvent.agentName())
          .setAttribute("simulation.score", agentScoreEvent.agentScore())
          .emit();
      case EpisodeAgentRankEvent agentRankEvent -> createCommon(agentRankEvent)
          .setAttribute("simulation.agent.name", agentRankEvent.agentName())
          .setAttribute("simulation.rank", agentRankEvent.agentRank())
          .emit();
      case EpisodeAgentWinLossEvent agentWinEvent -> createCommon(agentWinEvent)
          .setAttribute("simulation.agent.name", agentWinEvent.agentName())
          .setAttribute("simulation.result", agentWinEvent.agentResult().name())
          .emit();
      case StepProfileEvent profileEvent -> createCommon(profileEvent)
          .setAttribute("simulation.profile.phase", profileEvent.phaseName())
          .setAttribute("simulation.profile.step", profileEvent.stepNumber())
          .setAttribute("simulation.profile.duration_ns", profileEvent.duration())
          .emit();
    }
  }

  @Override
  public void close() {
    loggerProvider.forceFlush().join(10, java.util.concurrent.TimeUnit.SECONDS);
    openTelemetry.close();
    exporter.close();
  }
}