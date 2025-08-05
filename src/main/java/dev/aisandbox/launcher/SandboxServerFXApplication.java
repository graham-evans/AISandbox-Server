/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher;

import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.fx.FXModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SandboxServerFXApplication extends Application {

  //    private ConfigurableApplicationContext context;
  private Parent rootNode;

  @Override
  public void init() throws Exception {
    log.info("Initialising application - FX");
  }

  @Override
  public void start(Stage stage) throws Exception {
    log.info("Starting application - FX");

    Parent root = FXMLLoader.load(getClass().getResource("/fx/simulation.fxml"));
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.setTitle("AI Sandbox");
    stage.getIcons()
        .add(new Image(SandboxServerFXApplication.class.getResourceAsStream("/images/AILogo.png")));
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping application");
    SimulationRunner runner = FXModel.INSTANCE.getRunner();
    if (runner != null) {
      runner.stopSimulation();
    }
  }
}
