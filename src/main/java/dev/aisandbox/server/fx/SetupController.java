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

import java.io.File;
import java.io.IOException;

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
                        parameterBox.getChildren().clear();
                        for (ParameterEnumInfo e : RuntimeUtils.listEnumParameters(newValue)) {
                            parameterBox.getChildren().add(createParameterEditor(newValue, e));
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
            } else if (outputVideoChoice.isSelected()) {
                model.getOutputRenderer().set(new MP4Output(new File("/test.mp4")));
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
