/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.engine.maths.bins;

import dev.aisandbox.server.engine.maths.BinContents;
import java.util.List;

public interface BinningEngine {

  List<BinContents> binValues(List<Double> values);

}
