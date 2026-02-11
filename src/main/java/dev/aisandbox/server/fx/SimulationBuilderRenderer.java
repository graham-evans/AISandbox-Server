/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Cell renderer for displaying SimulationBuilder objects in a ListView.
 */
public class SimulationBuilderRenderer implements
    Callback<ListView<SimulationBuilder>, ListCell<SimulationBuilder>> {

  @Override
  public ListCell<SimulationBuilder> call(ListView<SimulationBuilder> simulationBuilderListView) {
    return new ListCell<>() {
      @Override
      public void updateItem(SimulationBuilder simulationBuilder, boolean empty) {
        super.updateItem(simulationBuilder, empty);
        if (empty || simulationBuilder == null) {
          setText(null);
        } else {
          setText(simulationBuilder.getSimulationName());
        }
      }
    };
  }
}
