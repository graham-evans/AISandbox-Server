/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

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
  private BufferedWriter writer;

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
    if (writer == null) {
      return;
    }
    for (String line : event.toJSON()) {
      try {
        writer.write(line);
      } catch (IOException e) {
        log.error("Failed to write telemetry event", e);
      }
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