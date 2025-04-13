package dev.aisandbox.server.engine.maths.bins;

import dev.aisandbox.server.engine.maths.BinContents;
import java.util.List;

public interface BinningEngine {

  List<BinContents> binValues(List<Double> values);

}
