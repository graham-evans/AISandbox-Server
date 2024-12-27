package dev.aisandbox.server.simulation.coingame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CoinScenario {
    SINGLE_21_2(new int[]{21}, 2),
    SINGLE_21_3(new int[]{21}, 3),
    DOUBLE_21_2(new int[]{21,21}, 2),
    DOUBLE_21_3(new int[]{21,21}, 3),
    NIM(new int[]{1,3,5,7},7);

    @Getter
    private final int[] rows;
    @Getter
    private final int max;
}
