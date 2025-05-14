/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget.axis;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 'Tight' variant of the Nice Axis algorithm.
 */
public class TightAxisScale implements AxisScale {

  @Getter
  private final double minimum;
  @Getter
  private final double maximum;
  @Getter
  private final List<Double> ticks;

  public TightAxisScale(double minimum, double maximum, int maxTicks) {
    this.minimum = minimum;
    this.maximum = maximum;
    NiceAxisScale niceAxisScale = new NiceAxisScale(minimum, maximum, maxTicks);
    ticks = niceAxisScale.getTicks().stream().filter(t -> (minimum <= t && t <= maximum))
        .collect(Collectors.toList());
  }

  @Override
  public String getValueString(double value) {
    return String.format("%.1f", value);
  }

  @Override
  public double getScaledValue(double value) {
    return (value - minimum) / (maximum - minimum);
  }

}
