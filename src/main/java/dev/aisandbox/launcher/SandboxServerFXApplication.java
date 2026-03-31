/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.launcher;

import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.fx.FXModel;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * JavaFX application for launching the AI Sandbox Server GUI.
 *
 * <p>This application initializes and displays the simulation interface, managing the JavaFX stage
 * and scene lifecycle. It loads the FXML layout from the resources and handles application startup
 * and shutdown, including stopping any running simulation.
 */
@Slf4j
public class SandboxServerFXApplication extends Application {

  @Override
  public void init() throws Exception {
    log.info("Initialising application - FX");
  }

  @Override
  public void start(Stage stage) throws Exception {
    log.info("Starting application - FX");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/simulation.fxml"));
    loader.setResources(ResourceBundle.getBundle("fx.simulation"));
    Parent root = loader.load();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle("AI Sandbox");
    stage.getIcons()
        .add(new Image(SandboxServerFXApplication.class.getResourceAsStream("/images/AILogo.png")));
    stage.setOpacity(0);
    stage.show();
    PauseTransition pause = new PauseTransition(Duration.millis(100));
    pause.setOnFinished(event -> {
      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
      stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2);
      stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2);
      stage.setOpacity(1);
    });
    pause.play();
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
