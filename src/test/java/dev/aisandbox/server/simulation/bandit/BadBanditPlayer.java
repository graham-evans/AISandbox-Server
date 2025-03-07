package dev.aisandbox.server.simulation.bandit;

import com.google.protobuf.GeneratedMessage;
import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class BadBanditPlayer  implements Agent {

    private final Random random = new Random();

    @Getter
    private final String agentName;

    private final boolean high;

    @Override
    public void send(GeneratedMessage o) {
        // do nothing
    }

    @Override
    public <T extends GeneratedMessage> T receive(GeneratedMessage state, Class<T> responseType) {
        BanditState banditState = (BanditState) state;
        if (responseType != BanditAction.class) {
            log.error("Asking for {} but I can only respond with BanditAction", responseType.getName());
            return null;
        } else {
            if (high) {
                return (T) BanditAction.newBuilder().setArm(Integer.MAX_VALUE).build();
            } else {
                return (T) BanditAction.newBuilder().setArm(Integer.MIN_VALUE).build();
            }

        }
    }

    @Override
    public void close() {

    }
}

