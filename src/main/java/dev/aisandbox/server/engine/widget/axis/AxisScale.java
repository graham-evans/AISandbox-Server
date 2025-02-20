package dev.aisandbox.server.engine.widget.axis;

import java.util.List;

public interface AxisScale {
    public double getMinimum();
    public double getMaximum();
    public double getScaledValue(double value);
    public String getValueString(double value);
    public List<Double> getTicks();
}
