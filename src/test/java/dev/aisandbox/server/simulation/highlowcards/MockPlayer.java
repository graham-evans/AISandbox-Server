package dev.aisandbox.server.simulation.highlowcards;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class MockPlayer implements Player {

    private final Random random = new Random();

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
        if (responseType != HighLowCardAction.class) {
            log.error("Asking for {} but I can only respond with HighLowCardAction", responseType.getName());
            return null;
        } else {
            if (random.nextBoolean()) {
                return (T) HighLowCardAction.newBuilder().setAction(HighLowChoice.HIGH).build();
            } else {
                return (T) HighLowCardAction.newBuilder().setAction(HighLowChoice.LOW).build();
            }
        }
    }

    @Override
    public void close() {

    }
}
