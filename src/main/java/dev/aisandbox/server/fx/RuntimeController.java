package dev.aisandbox.server.fx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;


@Slf4j
public class RuntimeController {

  FXModel model = FXModel.INSTANCE.getInstance();

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="logArea"
  private TextArea logArea; // Value injected by FXMLLoader

  private SetupController setupController;

  private SimulationRunner runner;

  @FXML
  void stopSimulationAction(ActionEvent event) {

  }

  @FXML
    // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    log.debug("Initializing RuntimeController");
    assert logArea
        != null : "fx:id=\"logArea\" was not injected: check your FXML file 'runtime.fxml'.";

    // setup logging control

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    FXLogbackAppender fxLogbackAppender = new FXLogbackAppender(logArea);
    fxLogbackAppender.setContext(lc);
    fxLogbackAppender.start();

    Logger logger = lc.getLogger("dev.aisandbox.server");
    logger.setLevel(Level.INFO);
    logger.addAppender(fxLogbackAppender);

    // start the simulation

    runner = SimulationSetup.setupSimulation(model.getSelectedSimulationBuilder().get(),
        model.getAgentCount().get(), model.getDefaultPort().get(), model.getOutputRenderer().get());
    runner.start();
    log.debug("Initialized RuntimeController");
  }

}
