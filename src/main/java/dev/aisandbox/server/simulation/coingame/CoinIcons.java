package dev.aisandbox.server.simulation.coingame;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class CoinIcons {

    public static final int ROW_WIDTH = 100;
    public static final int ROW_HEIGHT = 50;
    public static final int COINS_WIDTH = 300;
    private static final int COIN_WIDTH = 78;
    private static final int COIN_HEIGHT = 40;

    public static BufferedImage[] getRowImages(int rowCount) {
        BufferedImage[] images = new BufferedImage[rowCount];
        for (int i = 0; i < rowCount; i++) {
            images[i] = new BufferedImage(ROW_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = images[i].createGraphics();
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, ROW_WIDTH, ROW_HEIGHT);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Row " + Integer.toString(i), 0, ROW_HEIGHT / 2);
        }
        return images;
    }

    public static BufferedImage[] getCoinImages(int cointCount) throws IOException {
        BufferedImage[] images = new BufferedImage[cointCount];
        // load coin image
        BufferedImage coinImage = ImageIO.read(CoinIcons.class.getResourceAsStream("/images/coins/gold.png"));
        log.info("loaded coins image of width {} and height {}", coinImage.getWidth(), coinImage.getHeight());
        for (int i = 0; i < cointCount; i++) {
            images[i] = new BufferedImage(COINS_WIDTH, ROW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = images[i].createGraphics();
            g.setColor(Color.yellow);
            g.fillRect(0, 0, ROW_WIDTH, ROW_HEIGHT);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Coins= " + Integer.toString(i), 0, ROW_HEIGHT / 2);
        }
        return images;
    }

    /**
     * Get the number of rows required to best draw a stack of coins into a triangle
     *
     * @param num
     * @return
     */
    public static int[] getTriangleRows(int num) {
        List<Integer> rows = new ArrayList<>();
        rows.add(0);
        int cursor = 0;
        int max = 0;
        for (int i = 0; i < num; i++) {
            log.info("Adding 1 to {}, cursor={}", rows, cursor);
            // add coin to the pile
            if (cursor < rows.size()) {
                rows.set(cursor, rows.get(cursor) + 1);
                cursor++;
            } else if (rows.getLast() == 2) {
                rows.add(1);
                cursor = 0;
            } else {
                rows.set(0, rows.get(0) + 1);
                cursor = 1;
            }

        }
        int[] rowsArray = rows.stream().mapToInt(i -> i).toArray();
        log.info("Result = {}", rowsArray);
        return rowsArray;
    }

}
