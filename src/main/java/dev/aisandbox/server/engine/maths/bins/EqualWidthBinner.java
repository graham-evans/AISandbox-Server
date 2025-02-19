package dev.aisandbox.server.engine.maths.bins;

import dev.aisandbox.server.engine.maths.BinContents;
import dev.aisandbox.server.engine.maths.StatisticsUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Binner which uses ceil(sqrt(n)) bins of equal width
 */
public class EqualWidthBinner implements BinningEngine {

    @Override
    public List<BinContents> binValues(List<Double> values) {
        // get minimum and maximum and the range
        Pair<Double,Double> minMax = StatisticsUtils.getMinMax(values);
        double minimumValue = minMax.getLeft();
        double maximumValue = minMax.getRight();
        // get the range between min and max
        double range = maximumValue - minimumValue;
        // get the number of bins
        int binCount = (int)Math.ceil(Math.sqrt((double)values.size()));
        // calculate the width of each bin
        double width = range / binCount;
        List<Double> binStarts = new ArrayList<>();
        List<Double> binEnds = new ArrayList<>();
        binStarts.add(minimumValue);
        for (int i=1;i<binCount;i++) {
            double split = minimumValue + width * i;
            binEnds.add(split);
            binStarts.add(split);
        }
        binEnds.add(maximumValue);
        // count bin contents
        return StatisticsUtils.getBinnedValues(values, binStarts, binEnds);
    }
}
