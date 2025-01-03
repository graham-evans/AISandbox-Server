package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameterUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                                        newValue.getMinPlayerCount(),
                                        newValue.getMaxPlayerCount(),
                                        getValueInRange(agentCounter.getValue(), newValue.getMinPlayerCount(), newValue.getMaxPlayerCount())));
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
        log.info("Starting simulation");
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
