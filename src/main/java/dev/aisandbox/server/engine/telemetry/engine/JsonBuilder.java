/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.telemetry.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Helper class to inject key / values into an existing ObjectNode with chaining.
 */
public class JsonBuilder {

  private final ObjectNode root;

  private JsonBuilder(ObjectNode root) {
    this.root = root;
  }

  public static JsonBuilder on(ObjectNode root) {
    return new JsonBuilder(root);
  }

  public JsonBuilder put(String value, String... keys) {
    ObjectNode current = root;
    for (int i = 0; i < keys.length - 1; i++) {
      JsonNode next = current.get(keys[i]);
      if (next == null || !next.isObject()) {
        next = current.putObject(keys[i]);
      }
      current = (ObjectNode) next;
    }
    current.put(keys[keys.length - 1], value);
    return this; // Return the builder for chaining
  }

  // Overloads for other types
  public JsonBuilder put(int value, String... keys) {
    return put(Integer.toString(value), keys);
  }

  public JsonBuilder put(boolean value, String... keys) {
  return put(Boolean.toString(value),keys);
  }

  public JsonBuilder put(double value, String... keys) {
    return put(Double.toString(value),keys);
  }

  public JsonBuilder put(long value, String... keys) {
    return put(Long.toString(value), keys);
  }

  public ObjectNode build() {
    return root;
  }
}
