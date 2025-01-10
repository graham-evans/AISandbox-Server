package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.output.*;
import dev.aisandbox.server.options.ParameterEnumInfo;
import dev.aisandbox.server.options.RuntimeUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SetupController {

    @Autowired
    List<SimulationBuilder> simulationBuilderList;

    @Autowired
    private ApplicationContext appContext;

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

    @Getter
    private SimulationPackage simulationPackageToLaunch = null;

    public static Node createParameterEditor(SimulationBuilder builder, ParameterEnumInfo propertyDescriptor) {
        BorderPane node = new BorderPane();
        // add label
        Label label = new Label(propertyDescriptor.parameterName());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        node.setCenter(label);
        // add editor
        ComboBox<String> editor = new ComboBox<>();
        editor.setItems(FXCollections.observableList(propertyDescriptor.enumValues()));
        editor.getSelectionModel().select(propertyDescriptor.parameterValue());
        editor.valueProperty().addListener((observable, oldValue, newValue) -> {
            RuntimeUtils.setEnumParameter(builder, propertyDescriptor.parameterName(), newValue);
        });
        node.setRight(editor);
        return node;
    }

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
                        for (ParameterEnumInfo e : RuntimeUtils.listEnumParameters(newValue)) {
                            parameterBox.getChildren().add(createParameterEditor(newValue, e));
                        }
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
        // get selected simulation builder
        SimulationBuilder builder = simulationList.getSelectionModel().getSelectedItem();

        if (builder != null) {
            // create output
            OutputRenderer out;
            if (outputScreenChoice.isSelected()) {
                out = new ScreenOutputRenderer();
            } else if (outputVideoChoice.isSelected()) {
                out = new MP4Output(new File("/test.mp4"));
            } else if (outputImageChoice.isSelected()) {
                out = new BitmapOutputRenderer();
            } else {
                out = new NullOutputRenderer();
            }
            // package the simulation ready to launch
            simulationPackageToLaunch = new SimulationPackage(builder, agentCounter.getValue(), 9000, out);
            // flip to runtime screen
            try {
                FXMLLoader loader = new FXMLLoader(SetupController.class.getResource("/fx/runtime.fxml"));
                //      loader.setResources(ResourceBundle.getBundle("dev.aisandbox.client.fx.UI"));
                loader.setControllerFactory(appContext::getBean);
                Parent root = loader.load();
                Window window = ((Button) event.getSource()).getScene().getWindow();
                window.getScene().setRoot(root);
            } catch (IOException e) {
                log.error("Error switching Javafx scenes", e);
            }
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

    public record SimulationPackage(SimulationBuilder builder, int agentCount, int defaultPort, OutputRenderer output) {
    }
}
