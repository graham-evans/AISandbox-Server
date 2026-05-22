/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

public interface TelemetryEngine {

  /**
   * Initialise the telemetry stream and write a
   * @param SessionID
   */
  void initialise(String SessionID);

  /**
   * Write a single telemetry event.
   *
   * <p>Note: events may be buffered before writing.
   * @param event the event to be written
   */
  void writeTelemetryEvent(TelemetryEvent event);

  /**
   * End the telemetry session and close any resources, may be called multiple times.
   */
  void close();
}
