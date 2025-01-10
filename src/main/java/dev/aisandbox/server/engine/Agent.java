package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public interface Agent {
    String getAgentName();
    void send(GeneratedMessage o);
 //   GeneratedMessage receive(GeneratedMessage state);
    <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType);
    void close();
}
