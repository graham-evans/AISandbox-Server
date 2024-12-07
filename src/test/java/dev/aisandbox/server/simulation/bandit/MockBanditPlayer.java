package dev.aisandbox.server.simulation.bandit;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockBanditPlayer implements Player {

    @Getter
    private final String playerName;

    @Override
    public void send(GeneratedMessage o) {
        // do nothing
    }

    @Override
    public <T extends GeneratedMessage> T recieve(GeneratedMessage state, Class<T> responseType) {
        if (responseType != BanditAction.class) {
            log.error("Asking for {} but I can only respond with BanditAction", responseType.getName());
            return null;
        } else {
            return (T) BanditAction.newBuilder().setArm(0).build();
        }
    }

    @Override
    public void close() {

    }
}
