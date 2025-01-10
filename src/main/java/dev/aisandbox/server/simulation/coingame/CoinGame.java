package dev.aisandbox.server.simulation.coingame;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import dev.aisandbox.server.simulation.coingame.proto.Signal;
import dev.aisandbox.server.simulation.highlowcards.HighLowCards;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CoinGame implements Simulation {

    private static final int MARGIN = 100;
    private static final int TEXT_HEIGHT = 280;
    private static final int TEXT_WIDTH = OutputConstants.HD_WIDTH - 3 * MARGIN - 920;
    private final List<Agent> agents;
    private final CoinScenario scenario;
    private final Theme theme;
    private final TextWidget textWidget;
    private final TextWidget textSnapshot;
    private int[] coins;
    private int maxPic = 2;
    private int currentPlayer = 0;
    private BufferedImage[] rowImages;
    private BufferedImage[] coinImages;
    private BufferedImage logo;

    public CoinGame(final List<Agent> agents, final CoinScenario scenario, final Theme theme) {
        this.agents = agents;
        this.scenario = scenario;
        this.theme = theme;
        // build the coin pile
        coins = new int[scenario.getRows().length];
        // set up the widgets
        textSnapshot = TextWidget.builder().width(200).height(200).theme(theme).build();
        textWidget = TextWidget.builder()
                .width(TEXT_WIDTH)
                .height(TEXT_HEIGHT)
                .fontHeight(24)
                .fontName("Ariel")
                .theme(theme)
                .build();
        // generate the images
        try {
            rowImages = CoinIcons.getRowImages(scenario.getRows().length);
            coinImages = CoinIcons.getCoinImages(Arrays.stream(scenario.getRows()).max().getAsInt());
        } catch (IOException e) {
            log.error("Error loading images", e);
        }
        // load logo
        try {
            logo = ImageIO.read(HighLowCards.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(OutputConstants.LOGO_WIDTH, OutputConstants.LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
        // reset the game to the initial stage
        reset();
    }

    private void reset() {
        // reset the number of coins in each pile
        System.arraycopy(scenario.getRows(), 0, coins, 0, scenario.getRows().length);
    }

    @Override
    public void step(OutputRenderer output) {
        // draw the current state
        output.display();
        log.debug("ask client {} to move", currentPlayer);
        CoinGameState currentState = generateCurrentState(Signal.PLAY);
        CoinGameAction action = agents.get(currentPlayer).receive(currentState, CoinGameAction.class);
        // try and make the move
        try {
            coins = makeMove(action.getSelectedRow(), action.getRemoveCount());
            textWidget.addText(agents.get(currentPlayer).getAgentName() + " takes " + action.getRemoveCount() + " from row " + action.getSelectedRow() + " leaving " + coins[action.getSelectedRow()] + ".");
            if (isGameFinished()) {
                // current player lost
                textWidget.addText(agents.get(currentPlayer).getAgentName() + " lost");
                informResult((currentPlayer + 1) % 2);
                output.display();
                reset();
            }
        } catch (IllegalCoinAction e) {
            log.error(e.getMessage());
            textWidget.addText(agents.get(currentPlayer).getAgentName() + " makes an illegal move.");
            // player has tried an illegal move - end the game
            informResult((currentPlayer + 1) % 2);
            output.display();
            reset();
        }
        currentPlayer++;
        if (currentPlayer >= agents.size()) {
            currentPlayer = 0;
        }
    }

    private void informResult(int winner) {
        Agent winAgent = agents.get(winner);
        Agent loseAgent = agents.get((winner + 1) % 2);
        winAgent.send(generateCurrentState(Signal.WIN));
        loseAgent.send(generateCurrentState(Signal.LOSE));
        textSnapshot.addText("Player [" + winner + "] wins");
    }

    private int[] makeMove(int row, int amount) throws IllegalCoinAction {
        // is the amount out of the allowed range
        if (amount < 1 || amount > scenario.getMax()) {
            throw new IllegalCoinAction("Must remove between 1 and " + scenario.getMax() + " coins.");
        }
        // is the row out of the allowed indexing range
        if (row < 0 || row >= coins.length) {
            if (coins.length == 1) {
                throw new IllegalCoinAction("Must select row 0.");
            } else {
                throw new IllegalCoinAction("Must select row from 0 to " + (coins.length - 1) + ".");
            }
        }
        // does the selected row have enough coins
        if (coins[row] < amount) {
            throw new IllegalCoinAction("Not enough coins in the selected row.");
        }
        // is the agent taking too many coins
        if (amount > maxPic) {
            throw new IllegalCoinAction("Taking more than the maximum amount of coins.");
        }
        // make the move
        int[] newCoins = Arrays.copyOf(coins, coins.length);
        newCoins[row] -= amount;
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
        return CoinGameState.newBuilder()
                .addAllCoinCount(Arrays.stream(coins).boxed().toList())
                .setRowCount(coins.length)
                .setMaxPick(maxPic)
                .setSignal(signal)
                .build();
    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        graphics2D.setColor(theme.getText());
        for (int i = 0; i < coins.length; i++) {
            graphics2D.drawImage(rowImages[i], MARGIN, MARGIN + i * CoinIcons.ROW_HEIGHT, null);
            graphics2D.drawImage(coinImages[coins[i]], MARGIN + CoinIcons.ROW_WIDTH, MARGIN + i * CoinIcons.ROW_HEIGHT, null);
        }
        graphics2D.drawImage(textWidget.getImage(), MARGIN * 2 + 720 + 200, MARGIN, null);
        graphics2D.drawImage(textSnapshot.getImage(), MARGIN * 2 + 720 + 200, 500, null);
        // draw logo
        graphics2D.drawImage(logo, OutputConstants.HD_WIDTH - OutputConstants.LOGO_WIDTH - MARGIN, OutputConstants.HD_HEIGHT - OutputConstants.LOGO_HEIGHT - MARGIN, null);

    }
}
