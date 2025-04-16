package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.output.OutputRenderer;
import dev.aisandbox.server.simulation.SimulationEnumeration;
import java.util.Arrays;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum FXModel {
  INSTANCE();

  // model vars
  @Getter
  private final ObjectProperty<SimulationBuilder> selectedSimulationBuilder =
      new SimpleObjectProperty<>();
  @Getter
  private final ObservableList<SimulationBuilder> simulations = FXCollections.observableArrayList();
  @Getter
  private final IntegerProperty agentCount = new SimpleIntegerProperty(1);
  @Getter
  private final IntegerProperty defaultPort = new SimpleIntegerProperty(9000);
  @Getter
  private final ObjectProperty<OutputRenderer> outputRenderer = new SimpleObjectProperty<>();

  FXModel() {
    // populate simulation list
    Arrays.stream(SimulationEnumeration.values()).map(SimulationEnumeration::getBuilder)
        .forEach(simulations::add);
    simulations.sort(new SimulationComparator());
  }

  public FXModel getInstance() {
    return INSTANCE;
  }

}
