package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Agent;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.Theme;
import dev.aisandbox.server.engine.output.OutputConstants;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.engine.widget.TextWidget;
import dev.aisandbox.server.simulation.bandit.model.Bandit;
import dev.aisandbox.server.simulation.bandit.model.BanditNormalEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditStdEnumeration;
import dev.aisandbox.server.simulation.bandit.model.BanditUpdateEnumeration;
import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditResult;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import dev.aisandbox.server.simulation.bandit.proto.Signal;
import dev.aisandbox.server.simulation.highlowcards.HighLowCards;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class BanditRuntime implements Simulation {

    //    private AverageRewardGraph averageRewardGraph;
//    private OptimalActionGraph optimalActionGraph;
    //   private BanditGraph banditGraph;
    private static final int MARGIN = 100;
    // initial parameters
    private final Agent agent;
    private final Random rand;
    private final int banditCount;
    private final int pullCount;
    private final BanditNormalEnumeration normal;
    private final BanditStdEnumeration std;
    private final BanditUpdateEnumeration updateRule;
    private final Theme theme;
    private int sessionStep = 0;
    private String sessionID = UUID.randomUUID().toString();
    private String episodeID;
    private BufferedImage logo;
    private List<Bandit> bandits = new ArrayList<>();
    private TextWidget logWidget = TextWidget.builder().width(400).height(300).build();

    public BanditRuntime(Agent agent, Random rand, int banditCount, int pullCount, BanditNormalEnumeration normal, BanditStdEnumeration std, BanditUpdateEnumeration updateRule, Theme theme) {
        // store parameters
        this.agent = agent;
        this.rand = rand;
        this.banditCount = banditCount;
        this.pullCount = pullCount;
        this.normal = normal;
        this.std = std;
        this.updateRule = updateRule;
        this.theme = theme;
        // load logo
        try {
            logo = ImageIO.read(HighLowCards.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(OutputConstants.LOGO_WIDTH, OutputConstants.LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
        // initialise bandits
        initialise();
    }

    @Override
    public void step(OutputRenderer output) {
        sessionStep++;
        log.debug("Starting step {}", sessionStep);
        // ask user which bandit to pull
        BanditAction action = agent.receive(getState(), BanditAction.class);
        int arm = action.getArm();
        log.debug("Received request to pull arm {}", arm);
        // todo - test for invalid request
        // get the score
        double score = bandits.get(arm).pull(rand);
        // log the action
        logWidget.addText(agent.getAgentName() + " selects bandit " + arm + " gets reward " + score);
        // should we reset
        boolean reset = sessionStep == pullCount;
        if (reset) {
            logWidget.addText("pull count reached, starting a new game.");
        }
        // update the screen
        output.display();
        // tell the user the result
        agent.send(BanditResult.newBuilder().setArm(arm).setScore(score).setSignal(reset ? Signal.RESET : Signal.CONTINUE).build());
        // update the bandits
        switch (updateRule) {
            case RANDOM:
                updateRandom();
                break;
            case EQUALISE:
                updateEqualise(arm);
                break;
            case FADE:
                updateFade(arm);
                break;
            default: // FIXED
                // no action
        }
        // reset?
        if (reset) {
            initialise();
        }
    }

    /**
     * Generate a state (protobuf) object based on the current state.
     *
     * @return the state of the current game
     */
    private BanditState getState() {
        BanditState.Builder builder = BanditState.newBuilder();
        builder.setSessionID(sessionID);
        builder.setEpisodeID(episodeID);
        builder.setBanditCount(bandits.size());
        builder.setPull(sessionStep);
        builder.setPullCount(pullCount);
        return builder.build();
    }

    @Override
    public void visualise(Graphics2D graphics2D) {
        graphics2D.setColor(theme.getBackground());
        graphics2D.fillRect(0, 0, OutputConstants.HD_WIDTH, OutputConstants.HD_HEIGHT);
        // draw logo
        graphics2D.drawImage(logo, OutputConstants.HD_WIDTH - OutputConstants.LOGO_WIDTH - MARGIN, OutputConstants.HD_HEIGHT - OutputConstants.LOGO_HEIGHT - MARGIN, null);
        // draw log window
        graphics2D.drawImage(logWidget.getImage(), 800, MARGIN, null);
        // draw ave reward
        //   graphics2D.drawImage(averageRewardGraph.getImage(), 100, 200, null);
        //   graphics2D.drawImage(optimalActionGraph.getGraph(900, 400), 100, 650, null);
        // draw bandits
        // Create Chart @ Margin,Margin
        graphics2D.setTransform(AffineTransform.getTranslateInstance(MARGIN, MARGIN));
        CategoryChart chart =
                new CategoryChartBuilder()
                        .width(600)
                        .height(400)
                        .title("Bandits")
                        .xAxisTitle("Bandit")
                        .yAxisTitle("Expected Result")
                        .theme(theme.getChartTheme())
                        .build();
        List<String> xAxisLabels = new ArrayList<>();
        List<Double> yAxisValues = new ArrayList<>();
        List<Double> errorValues = new ArrayList<>();
        for (int i = 0; i < bandits.size(); i++) {
            xAxisLabels.add(Integer.toString(i));
            yAxisValues.add(bandits.get(i).getMean());
            errorValues.add(bandits.get(i).getStd());
        }
        chart.addSeries("Bandits", xAxisLabels, yAxisValues, errorValues);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Scatter);
        chart.getStyler().setLegendVisible(false);
        chart.paint(graphics2D, 600, 400);

    }

    /**
     * initialise or reset the bandit state, creating new bandits as needed.
     */
    public void initialise() {
        // clear the bandits
        bandits.clear();
        for (int i = 0; i < banditCount; i++) {
            bandits.add(new Bandit(normal.getNormalValue(rand), std.getValue()));
        }
        sessionStep = 0;
        episodeID = UUID.randomUUID().toString();
    }

    /**
     * Update the bandits by moving each mean N(0,0.1)
     */
    public void updateRandom() {
        for (Bandit b : bandits) {
            b.setMean(b.getMean() + rand.nextGaussian() * 0.001);
        }
    }

    /**
     * Update the bandits by moving the chosen bandit by -0.001
     *
     * @param chosen the index of the chosen bandit
     */
    public void updateFade(int chosen) {
        Bandit target = bandits.get(chosen);
        target.setMean(target.getMean() - 0.001);
    }

    /**
     * Update the bandits by moving the mean of the chosen bandit by -0.001 and all others by +0.001/k
     *
     * @param chosen the index of the chosen bandit
     */
    public void updateEqualise(int chosen) {
        double reward = 0.001 / bandits.size();
        for (int i = 0; i < bandits.size(); i++) {
            Bandit b = bandits.get(i);
            if (i == chosen) {
                b.setMean(b.getMean() - 0.001);
            } else {
                b.setMean(b.getMean() + reward);
            }
        }
    }
}
