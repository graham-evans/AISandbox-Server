/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

public class InvalidCascadeAction extends RuntimeException {
    public InvalidCascadeAction(String message) {
        super(message);
    }
}
