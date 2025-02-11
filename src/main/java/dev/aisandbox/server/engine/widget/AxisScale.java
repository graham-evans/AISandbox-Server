package dev.aisandbox.server.engine.widget;

import java.util.List;

public interface AxisScale {
    public double getMinimum();
    public double getMaximum();
    public double getScaledValue(double value);
    public List<Double> getTicks();
}
