package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPlayer implements Player {

    PlayerThread playerThread;
    @Getter
    String playerName;

    public NetworkPlayer(String playerName, int defaultPort) {
        this.playerName = playerName;
        playerThread = new PlayerThread(playerName, defaultPort);
        playerThread.start();
    }

    @Override
    public void send(GeneratedMessage o) {
        playerThread.sendMessage(o);
    }

    @Override
    public <T extends GeneratedMessage> T recieve(GeneratedMessage state, Class<T> responseType) {
        return (T) playerThread.sendMessageGetResponse(state);
    }

    @Override
    public void close() {

    }

}
