package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.simulation.coingame.CoinIcons;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoinTests {

    @Test
    public void voidTestCoins() throws IOException {
        BufferedImage[] coins = CoinIcons.getCoinImages(30);
        assertEquals(30, coins.length, "Incorrect number of coins");
    }

    @Test
    public void triangleTest0() {
        assertArrayEquals(new int[] {0},CoinIcons.getTriangleRows(0));
    }

    @Test
    public void triangleTest1() {
        assertArrayEquals(new int[] {1},CoinIcons.getTriangleRows(1));
    }

    @Test
    public void triangleTest2() {
        assertArrayEquals(new int[] {2},CoinIcons.getTriangleRows(2));
    }
    @Test
    public void triangleTest3() {
        assertArrayEquals(new int[] {1,2},CoinIcons.getTriangleRows(3));
    }
}
