package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;

public interface Player {
    String getPlayerName();
    void send(GeneratedMessage o);
 //   GeneratedMessage receive(GeneratedMessage state);
    <T extends GeneratedMessage> T recieve(GeneratedMessage state, Class<T> responseType);
    void close();
}
