package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.output.BitmapOutputRenderer;
import dev.aisandbox.server.engine.output.NullOutputRenderer;
import dev.aisandbox.server.engine.output.ScreenOutputRenderer;
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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j

public class SetupController {

    FXModel model = FXModel.INSTANCE.getInstance();

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

    public static Node createParameterEditor(SimulationBuilder builder, String parameterName, String parameterDescription) {
        BorderPane node = new BorderPane();
        // add label
        Label label = new Label(parameterName);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        node.setCenter(label);
        // TODO - this assumes that all params are enums
        Class<?> targetClass = RuntimeUtils.getParameterClass(builder, parameterName);
        if (targetClass.isEnum()) {
            // create list of values
            List<String> enumNames = Arrays.stream(targetClass.getEnumConstants()).map(Object::toString).toList();
            // add editor
            ComboBox<String> editor = new ComboBox<>();
            editor.setItems(FXCollections.observableList(enumNames));
            editor.getSelectionModel().select(RuntimeUtils.getParameterValue(builder, parameterName).toString());
            editor.valueProperty().addListener((observable, oldValue, newValue) -> {
                RuntimeUtils.setParameterValue(builder, parameterName, newValue);
            });
            node.setRight(editor);
        }
        return node;
    }

    @FXML
    void initialize() {
        // FX assertions
        assert agentCounter != null : "fx:id=\"agentCounter\" was not injected: check your FXML file 'simulation.fxml'.";
        assert outputImageChoice != null : "fx:id=\"outputImageChoice\" was not injected: check your FXML file 'simulation.fxml'.";
        assert outputScreenChoice != null : "fx:id=\"outputScreenChoice\" was not injected: check your FXML file 'simulation.fxml'.";
        assert outputVideoChoice != null : "fx:id=\"outputVideoChoice\" was not injected: check your FXML file 'simulation.fxml'.";
        assert parameterBox != null : "fx:id=\"parameterBox\" was not injected: check your FXML file 'simulation.fxml'.";
        assert simDescription != null : "fx:id=\"simDescription\" was not injected: check your FXML file 'simulation.fxml'.";
        assert simulationList != null : "fx:id=\"simulationList\" was not injected: check your FXML file 'simulation.fxml'.";
        // bind simulation list to model
        simulationList.setItems(model.getSimulations());
        simulationList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        model.getSelectedSimulationBuilder().bind(simulationList.getSelectionModel().selectedItemProperty());
        // disable agent counter until builder is selected
//        model.getAgentCount().bind(agentCounter.valueProperty());
        agentCounter.setDisable(true);
        agentCounter.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        1,
                        1));

        // initialise the agent counter
        /*   */
        // set builder list renderer
        simulationList.setCellFactory(new SimulationBuilderRenderer());
        // update when selecting a new builder
        simulationList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
                {
                    if (newValue != null) {
                        // update description
                        simDescription.setText(newValue.getDescription());
                        // update agent count options
                        model.getAgentCount().unbind();
                        agentCounter.setValueFactory(
                                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                                        newValue.getMinAgentCount(),
                                        newValue.getMaxAgentCount(),
                                        getValueInRange(agentCounter.getValue(), newValue.getMinAgentCount(), newValue.getMaxAgentCount())));
                        agentCounter.setDisable(false);
                        model.getAgentCount().bind(agentCounter.valueProperty());
                        // populate the parameters with editor boxes
                        List<String> parameterNames = new ArrayList<>(newValue.getParameters().keySet());
                        Collections.sort(parameterNames);
                        parameterBox.getChildren().clear();
                        for (String parameterName : parameterNames) {
                            parameterBox.getChildren().add(createParameterEditor(newValue, parameterName, newValue.getParameters().get(parameterName)));
                        }
                    } else {
                        simDescription.setText("");
                        model.getAgentCount().unbind();
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
        if (model.getSelectedSimulationBuilder().get() != null) {
            // store output in model
            if (outputScreenChoice.isSelected()) {
                model.getOutputRenderer().set(new ScreenOutputRenderer());
            } else if (outputImageChoice.isSelected()) {
                model.getOutputRenderer().set(new BitmapOutputRenderer());
            } else {
                model.getOutputRenderer().set(new NullOutputRenderer());
            }
            // flip to runtime screen
            try {
                FXMLLoader loader = new FXMLLoader(SetupController.class.getResource("/fx/runtime.fxml"));
                //      loader.setResources(ResourceBundle.getBundle("dev.aisandbox.client.fx.UI"));
                //        loader.setControllerFactory(appContext::getBean);
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

}
