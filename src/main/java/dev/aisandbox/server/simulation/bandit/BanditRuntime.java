package dev.aisandbox.server.simulation.bandit;

import dev.aisandbox.server.engine.Player;
import dev.aisandbox.server.engine.Simulation;
import dev.aisandbox.server.engine.output.OutputRenderer;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_HEIGHT;
import static dev.aisandbox.server.engine.output.OutputConstants.LOGO_WIDTH;

@Slf4j
public class BanditRuntime implements Simulation {

    // initial parameters
    private final Player player;
    private final Random rand;
    private final int banditCount;
    private final int pullCount;
    private final BanditNormalEnumeration normal;
    private final BanditStdEnumeration std;
    private final BanditUpdateEnumeration updateRule;

    private int sessionStep = 0;

    private int iteration;
    private BufferedImage logo;
//    private AverageRewardGraph averageRewardGraph;
//    private OptimalActionGraph optimalActionGraph;
    //   private BanditGraph banditGraph;

    private List<Bandit> bandits = new ArrayList<>();

    public BanditRuntime(Player player, Random rand, int banditCount, int pullCount, BanditNormalEnumeration normal, BanditStdEnumeration std, BanditUpdateEnumeration updateRule) {
        // store parameters
        this.player = player;
        this.rand = rand;
        this.banditCount = banditCount;
        this.pullCount = pullCount;
        this.normal = normal;
        this.std = std;
        this.updateRule = updateRule;
        // load logo
        try {
            logo = ImageIO.read(HighLowCards.class.getResourceAsStream("/images/AILogo.png"));
        } catch (Exception e) {
            log.error("Error loading logo", e);
            logo = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
        // initialise bandits
        initialise();
    }

    @Override
    public void step(OutputRenderer output) {
        sessionStep++;
        log.debug("Starting step {}", sessionStep);
        // ask user which bandit to pull
        BanditAction action = player.recieve(getState(), BanditAction.class);
        int arm = action.getArm();
        log.debug("Received request to pull arm {}", arm);
        // todo - test for invalid request
        // get the score
        double score = bandits.get(action.getArm() - 1).pull(rand);
        // should we reset
        boolean reset = sessionStep == pullCount;
        // tell the user the result
        player.send(BanditResult.newBuilder().setArm(arm).setScore(score).setSignal(reset ? Signal.RESET : Signal.CONTINUE).build());
        // update the screen
        output.display();
        // reset?
        if (reset) {
            initialise();
        }
    }

    private BanditState getState() {
        BanditState.Builder builder = BanditState.newBuilder();
        builder.setBanditCount(bandits.size());
        builder.setPull(sessionStep);
        builder.setPullCount(pullCount);
        return builder.build();
    }


    @Override
    public void visualise(Graphics2D graphics2D) {
        // draw logo
        graphics2D.drawImage(logo, 100, 50, null);
        // draw ave reward
     //   graphics2D.drawImage(averageRewardGraph.getImage(), 100, 200, null);
     //   graphics2D.drawImage(optimalActionGraph.getGraph(900, 400), 100, 650, null);
        // draw bandits
     //   graphics2D.drawImage(banditGraph.getImage(), 1000, 200, null);
    }

    public void initialise() {
        // clear the bandits
        bandits.clear();
        for (int i = 0; i < banditCount; i++) {
            bandits.add(new Bandit(normal.getNormalValue(rand), std.getValue()));
        }
        sessionStep = 0;
//        currentSession = new BanditSession(rand, banditCount, normal, std);
//        averageRewardGraph = new AverageRewardGraph(900, 400, pullCount);
//        optimalActionGraph = new OptimalActionGraph(pullCount);
//        banditGraph = new BanditGraph(800, 400);
//        banditGraph.setBandits(currentSession.getBandits());
    }

  /*  @Override
    public RuntimeResponse advance() throws AgentException, SimulationException {
        ProfileStep profileStep = new ProfileStep();
        BanditRequest request = new BanditRequest();
        request.setHistory(history);
        request.setSessionID(currentSession.getSessionID());
        request.setBanditCount(banditCount);
        request.setPullCount(pullCount);
        request.setPull(iteration);
        log.info("Requesting next pull");
        BanditResponse response = agent.postRequest(request, BanditResponse.class);
        profileStep.addStep("Network");
        // resolve the response
        // TODO - check if arm exists (array out of bounds?)
        history = new BanditRequestHistory();
        history.setSessionID(currentSession.getSessionID());
        history.setChosenBandit(response.getArm());
        double reward = currentSession.activateBandit(response.getArm());
        history.setReward(reward);
        // was this the best move?
        boolean best = currentSession.isBestMean(response.getArm());
        // store result
        averageRewardGraph.addReward(iteration, reward);
        optimalActionGraph.addReward(iteration, best ? 100.0 : 0.0);
        // update bandits
        switch (updateRule) {
            case RANDOM:
                currentSession.updateRandom();
                break;
            case EQUALISE:
                currentSession.updateEqualise(response.getArm());
                break;
            case FADE:
                currentSession.updateFade(response.getArm());
                break;
            default: // FIXED
                // no action
        }
        profileStep.addStep("Simulation");
        // draw screen
        BufferedImage image = null;
        if (!skipGraphics || (iteration == 0)) {
            image = OutputTools.getWhiteScreen();
            Graphics2D graphics2D = image.createGraphics();
            // draw logo
            graphics2D.drawImage(logo, 100, 50, null);
            // draw ave reward
            graphics2D.drawImage(averageRewardGraph.getImage(), 100, 200, null);
            graphics2D.drawImage(optimalActionGraph.getGraph(900, 400), 100, 650, null);
            // draw bandits
            graphics2D.drawImage(banditGraph.getImage(), 1000, 200, null);
        }
        profileStep.addStep("Graphics");
        // check for end of run
        iteration++;
        if (iteration == pullCount) {
            // reset run
            iteration = 0;
            currentSession = new BanditSession(rand, banditCount, normal, std);
            banditGraph.setBandits(currentSession.getBandits());
        }
        profileStep.addStep("Simulation");
        return new RuntimeResponse(profileStep, image);
    }

    @Override
    public void writeStatistics(File statisticsOutputFile) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(statisticsOutputFile));
            out.println("Step,Ave Reward,% Optimal Action");
            for (int i = 0; i < pullCount; i++) {
                out.print(i);
                out.print(",");
                out.print(averageRewardGraph.getAveRewards()[i]);
                out.print(",");
                out.println(optimalActionGraph.getAveRewards()[i]);
            }
            out.close();
        } catch (IOException e) {
            log.warn("Error writing statistics", e);
        }
    }


   */
}
