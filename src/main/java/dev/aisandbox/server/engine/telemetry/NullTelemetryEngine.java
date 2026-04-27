/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry;

public class NullTelemetryEngine implements TelemetryEngine {

    @Override
    public void initialise(String SessionID) {

    }

    @Override
    public void writeTelemetryEvent(TelemetryEvent event) {

    }

    @Override
    public void close() {

    }
}
