package dev.aisandbox.server.fx;

import dev.aisandbox.server.engine.SimulationBuilder;

import java.util.Comparator;

public class SimulationComparator implements Comparator<SimulationBuilder> {
    @Override
    public int compare(SimulationBuilder o1, SimulationBuilder o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
