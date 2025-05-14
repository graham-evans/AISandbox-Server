/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public interface Agent {

  String getAgentName();

  void send(GeneratedMessage o);

  //   GeneratedMessage receive(GeneratedMessage state);
  <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType);

  void close();
}
