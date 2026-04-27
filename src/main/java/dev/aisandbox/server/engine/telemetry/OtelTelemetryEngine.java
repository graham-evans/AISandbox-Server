/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * Telemetry engine that forwards events to an OpenTelemetry collector via OTLP/HTTP.
 */
@Slf4j
public class OtelTelemetryEngine implements TelemetryEngine {

    private final OpenTelemetrySdk openTelemetry;
    private final SdkLoggerProvider loggerProvider;
    private final Logger logger;

    public OtelTelemetryEngine(String collectorUrl) {
        OtlpHttpLogRecordExporter exporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(collectorUrl)
                // .addHeader("Authorization", "Bearer <token>")
                .build();

        loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(exporter).build())
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setLoggerProvider(loggerProvider)
                .build();

        logger = openTelemetry.getLogsBridge().get("dev.aisandbox.server");
    }

    @Override
    public void initialise(String sessionID) {
    }

    @Override
    public void writeTelemetryEvent(TelemetryEvent event) {
        event.emit(logger);
    }

    @Override
    public void close() {
        loggerProvider.forceFlush().join(10, java.util.concurrent.TimeUnit.SECONDS);
        openTelemetry.close();
    }
}