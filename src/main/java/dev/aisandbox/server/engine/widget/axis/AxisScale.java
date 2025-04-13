package dev.aisandbox.server.engine.widget.axis;

import java.util.List;

public interface AxisScale {

  double getMinimum();

  double getMaximum();

  double getScaledValue(double value);

  String getValueString(double value);

  List<Double> getTicks();
}
