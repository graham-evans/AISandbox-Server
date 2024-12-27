package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.RollingPieChart;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.engine.widget.RollingLabelFrequencyStatistics;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import dev.aisandbox.server.simulation.coingame.proto.Signal;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CoinGame implements Simulation {

    private static final int MARGIN = 100;
    private static final int TEXT_HEIGHT = 280;
    private static final int TEXT_WIDTH = OutputConstants.HD_WIDTH - 3 * MARGIN - 920;
    private final List<Player> players;
    private final CoinScenario scenario;
    private final Theme theme;
    private final TextWidget textWidget;
    private int[] coins;
    private int currentPlayer = 0;
    private final RollingLabelFrequencyStatistics statistics = new RollingLabelFrequencyStatistics(100);
    private final TextWidget textSnapshot = statistics.createSummaryWidgetBuilder().width(200).height(200).build();

    public CoinGame(final List<Player> players, final CoinScenario scenario, final Theme theme) {
        this.players = players;
        this.scenario = scenario;
        this.theme = theme;
        coins = new int[scenario.getRows().length];
        textWidget = TextWidget.builder()
                .width(TEXT_WIDTH)
                .height(TEXT_HEIGHT)
                .fontHeight(24)
                .fontName("Ariel")
                .theme(theme)
                .build();
        reset();
    }

    private void reset() {
        // reset the number of coins in each pile
        System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    }

    @Override
    public void step(OutputRenderer output) {
        // todo add extra frame to begin
        log.debug("ask client {} to move", currentPlayer);
        CoinGameState currentState = generateCurrentState(Signal.PLAY);
        CoinGameAction action = players.get(currentPlayer).recieve(currentState, CoinGameAction.class);
        // try and make the move
        try {
            coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
            textWidget.addText(players.get(currentPlayer).getPlayerName() + " takes " + action.getRemoveCount() + " from row " + action.getSelectedRow());
            if (isGameFinished()) {
                // current player lost
                textWidget.addText(players.get(currentPlayer).getPlayerName() + " lost");
                informResult((currentPlayer + 1) % 2);
                output.display();
                reset();
            } else {
                output.display();
            }
        } catch (IllegalCoinAction e) {
            log.error(e.getMessage());
            textWidget.addText(players.get(currentPlayer).getPlayerName() + " makes an illegal move.");
            // player has tried an illegal move - end the game
            informResult((currentPlayer + 1) % 2);
            output.display();
            reset();
        }
        currentPlayer++;
        if (currentPlayer >= players.size()) {
            currentPlayer = 0;
        }
    }

    private void informResult(int winner) {
        Player winPlayer = players.get(winner);
        Player losePlayer = players.get((winner + 1) % 2);
        winPlayer.send(generateCurrentState(Signal.WIN));
        losePlayer.send(generateCurrentState(Signal.LOSE));
        statistics.addWinner(winPlayer.getPlayerName());
    }

    private int[] makeMove(int row, int amount) throws IllegalCoinAction {
        // is the amount out of the allowed range
        if (amount < 1 || amount > scenario.getMax()) {
            throw new IllegalCoinAction("Must remove between 1 and " + scenario.getMax() + " coins.");
        }
        // is the row out of the allowed indexing range
        if (row < 1 || row > coins.length) {
            if (coins.length == 1) {
                throw new IllegalCoinAction("Must select row 1.");
            } else {
                throw new IllegalCoinAction("Must select row from 1 to " + coins.length + ".");
            }
        }
        // does the selected row have enough coins
        if (coins[row-1] < amount) {
            throw new IllegalCoinAction("Not enough coins in the selected row.");
        }
        // make the move
        int[] newCoins = Arrays.copyOf(coins, coins.length);
        newCoins[row-1] -= amount;
        return newCoins;
    }

    private boolean isGameFinished() {
        for (int row = 0; row < scenario.getRows().length; row++) {
            if (coins[row] > 0) {
                return false;
            }
        }
        return true;
    }

    private CoinGameState generateCurrentState(Signal signal) {
        return CoinGameState.newBuilder().setSignal(signal).build();
    }


    @Override
    public void close() {

    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        graphics2D.setColor(theme.getText());
        for (int i = 0; i < coins.length; i++) {
            graphics2D.drawString("Row " + (i + 1), MARGIN, MARGIN + i * 30);
            graphics2D.drawString(Integer.toString(coins[i]), MARGIN + 50, MARGIN + i * 30);
        }
        graphics2D.drawImage(textWidget.getImage(), MARGIN * 2 + 720 + 200, MARGIN, null);
        graphics2D.drawImage(textSnapshot.getImage(), MARGIN * 2 + 720 + 200, 500, null);
    }
}
