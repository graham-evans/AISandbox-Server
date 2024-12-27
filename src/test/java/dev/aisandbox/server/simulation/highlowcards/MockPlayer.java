package dev.aisandbox.server.simulation.highlowcards;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.simulation.highlowcards.proto.ClientAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class MockPlayer implements Player {

    private Random random = new Random();

    @Override
    public String getPlayerName() {
        return "Mock Player";
    }

    @Override
    public void send(GeneratedMessage o) {
        // send message to player - ignore it
    }

    @Override
    public <T extends GeneratedMessage> T recieve(GeneratedMessage state, Class<T> responseType) {
        if (responseType != ClientAction.class) {
            log.error("Asking for {} but I can only respond with ClientAction", responseType.getName());
            return null;
        } else {
            if (random.nextBoolean()) {
                return (T) ClientAction.newBuilder().setAction(HighLowChoice.HIGH).build();
            } else {
                return (T) ClientAction.newBuilder().setAction(HighLowChoice.LOW).build();
            }
        }
    }

    @Override
    public void close() {

    }
}
