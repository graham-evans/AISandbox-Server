package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public interface Player {
    String getPlayerName();
    void send(GeneratedMessage o);
    GeneratedMessage receive(GeneratedMessage state);
    void close();
}
