/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationRunner;
import dev.aisandbox.server.engine.SimulationSetup;
import dev.aisandbox.server.engine.exception.SimulationSetupException;
import dev.aisandbox.server.engine.output.FXRenderer;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RuntimeController {

  FXModel model = FXModel.INSTANCE.getInstance();
  String outputText = "";
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;
  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;
  @FXML // Text area for log output
  private TextArea logArea; // Value injected by FXMLLoader
  @FXML
  private BorderPane borderPane;
  @FXML
  private Button stopSimulationButton;

  private SimulationRunner runner;

  ImageView imageView;

  @FXML
  void stopSimulationAction(ActionEvent event) {
    runner.stopSimulation();
    Stage stage = (Stage) stopSimulationButton.getScene().getWindow();
    stage.close();
  }

  public void updateOutput(String line) {
    outputText += line + "\n";
    Platform.runLater(() -> logArea.setText(outputText));
  }

  public void updateImage(BufferedImage image) {
    Image fximage = SwingFXUtils.toFXImage(image, null);
    Platform.runLater(() -> imageView.setImage(fximage));
  }

  @FXML
  void initialize() { // This method is called by the FXMLLoader when initialization is complete
    log.debug("Initializing RuntimeController");
    assert logArea
        != null : "fx:id=\"logArea\" was not injected: check your FXML file 'runtime.fxml'.";
    // setup the image
    imageView = new ImageView(
        new Image(RuntimeController.class.getResourceAsStream("/images/backgrounds/testcard.png")));
    imageView.setPreserveRatio(true);

    borderPane.setCenter(new WrappedImageView(imageView));

    // start the simulation
    try {
      FXRenderer renderer = new FXRenderer(this);

      runner = SimulationSetup.setupSimulation(model.getSelectedSimulationBuilder().get(),
          model.getAgentCount().get(), model.getDefaultPort().get(), false, renderer, -1L);
      runner.start();
      log.debug("Initialized RuntimeController");
    } catch (SimulationSetupException e) {
      logArea.setText("Error initialising simulation.");
      log.error("Error setting up simulation", e);
    }
  }

}
