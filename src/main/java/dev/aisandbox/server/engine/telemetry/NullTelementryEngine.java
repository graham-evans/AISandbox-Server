/*
 *
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 *
 */

package dev.aisandbox.server.engine.telemetry;

public class NullTelementryEngine implements TelemetryEngine {

    @Override
    public void initialise() {

    }

    @Override
    public void writeTelementryEvent(TelemetryEvent event) {

    }

    @Override
    public void close() {

    }
}
