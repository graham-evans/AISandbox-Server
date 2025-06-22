/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;


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
  void initialize() { // This method is called by the FXMLLoader when initialization is complete
    log.debug("Initializing RuntimeController");
    assert logArea
        != null : "fx:id=\"logArea\" was not injected: check your FXML file 'runtime.fxml'.";

    // setup logging control

  /*  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    FXLogbackAppender fxLogbackAppender = new FXLogbackAppender(logArea);
    fxLogbackAppender.setContext(lc);
    fxLogbackAppender.start();

    Logger logger = lc.getLogger("dev.aisandbox.server");
    logger.setLevel(Level.INFO);
    logger.addAppender(fxLogbackAppender);
*/
    // start the simulation
    try {
      runner = SimulationSetup.setupSimulation(model.getSelectedSimulationBuilder().get(),
          model.getAgentCount().get(), model.getDefaultPort().get(),
          model.getOutputRenderer().get());
      runner.start();
      log.debug("Initialized RuntimeController");
    } catch (SimulationSetupException e) {
      // todo - alert user via UI
      log.error("Error setting up simulation", e);
    }
  }

}
