package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.*;
import dev.aisandbox.server.engine.output.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FXController {

    @Autowired
    List<SimulationBuilder> simulationBuilderList;

    @FXML
    private VBox parameterBox;

    @FXML
    private TextArea simDescription;
    @FXML
    private Spinner<Integer> agentCounter;

    @FXML
    private RadioButton outputImageChoice;

    @FXML
    private RadioButton outputScreenChoice;

    @FXML
    private RadioButton outputVideoChoice;

    @FXML
    private ListView<SimulationBuilder> simulationList;

    @FXML
    void initialize() {
        // FX assertions
        assert parameterBox != null : "fx:id=\"parameterBox\" was not injected: check your FXML file 'simulation.fxml'.";

        // spring assertions
        assert simulationBuilderList != null : "simulationBuilderList was null";

        // initialise the components
        agentCounter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        agentCounter.setDisable(true);
        Collections.sort(simulationBuilderList, new SimulationComparator());
        simulationList.getItems().addAll(simulationBuilderList);
        simulationList.setCellFactory(new SimulationBuilderRenderer());
        simulationList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
                {
                    if (newValue != null) {
                        // update description
                        simDescription.setText(newValue.getDescription());
                        // update agent count options
                        agentCounter.setValueFactory(
                                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                                        newValue.getMinAgentCount(),
                                        newValue.getMaxAgentCount(),
                                        getValueInRange(agentCounter.getValue(), newValue.getMinAgentCount(), newValue.getMaxAgentCount())));
                        agentCounter.setDisable(false);
                        // populate the parameters with editor boxes
                        parameterBox.getChildren().clear();
                        parameterBox.getChildren().addAll(SimulationParameterUtils.getParameters(newValue).stream().map(parameter -> SimulationParameterUtils.createParameterEditor(parameter)).toList());
                    } else {
                        simDescription.setText("");
                        agentCounter.setDisable(true);
                        parameterBox.getChildren().clear();
                    }
                }
        );
        // select the first simulation in the list
        simulationList.getSelectionModel().select(0);
    }

    @FXML
    void startSimulation(ActionEvent event) {
        // get selected simulation
        SimulationBuilder builder = simulationList.getSelectionModel().getSelectedItem();
        if (builder != null) {
            log.info("Starting simulation {} with {} agents", builder.getSimulationName(), agentCounter.getValue());
            List<Player> players = Arrays.stream(
                    builder.getAgentNames(agentCounter.getValue())).map(s -> (Player) new NetworkPlayer(s, 9000)).toList();
            // create simulation
            Simulation sim = builder.build(players, Theme.DEFAULT);
            // create output
            OutputRenderer out;
            if (outputScreenChoice.isSelected()) {
                out = new ScreenOutputRenderer(sim);
            } else if (outputVideoChoice.isSelected()) {
                out = new MP4Output(sim, new File("/test.mp4"));
            } else if (outputImageChoice.isSelected()) {
                out = new BitmapOutputRenderer(sim);
            } else {
                out = new NullOutputRenderer();
            }
            // start runner
            SimulationRunner runner = new SimulationRunner(sim,out,players);
            // switch screens
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fx/runtime.fxml"));
                Window window = ((Button) event.getSource()).getScene().getWindow();
                window.getScene().setRoot(root);
            } catch (IOException e) {
                log.error("Error loading fxml", e);
            }
            // TODO switch screens
            runner.start();
        }
    }

    /**
     * Return a value, adapted to fit within an existing range.
     *
     * @param value the original value
     * @param min   the minimum new value
     * @param max   the maximum new value
     * @return the adapted value.
     */
    private int getValueInRange(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }


}
