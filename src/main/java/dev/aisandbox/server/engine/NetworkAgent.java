package dev.aisandbox.server.engine;

import com.google.protobuf.GeneratedMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkAgent implements Agent {

    AgentThread agentThread;
    @Getter
    String agentName;

    public NetworkAgent(String agentName, int defaultPort) {
        this.agentName = agentName;
        agentThread = new AgentThread(agentName, defaultPort);
        agentThread.start();
    }

    @Override
    public void send(GeneratedMessage o) {
        agentThread.sendMessage(o);
    }

    @Override
    public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
        return (T) agentThread.sendMessageGetResponse(state);
    }

    @Override
    public void close() {

    }

}
