/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.widget.axis;

import java.util.List;

public interface AxisScale {

  double getMinimum();

  double getMaximum();

  double getScaledValue(double value);

  String getValueString(double value);

  List<Double> getTicks();
}
