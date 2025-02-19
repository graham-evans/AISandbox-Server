package dev.aisandbox.server.engine.maths;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class StatisticsUtils {

    /**
     * Get the minimum and maximum of a list of numbers.
     * <p>
     * For an empty list an exception will be thrown
     *
     * @param values the list to analyse
     * @return a min/max pair
     */
    public static Pair<Double, Double> getMinMax(final List<Double> values) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (final double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return Pair.of(min, max);
    }

    public static List<BinContents> getBinnedValues(final List<Double> values, final List<Double> binStarts, final List<Double> binEnds) {
        assert binStarts.size() == binEnds.size();
        int[] bincounts = new int[binStarts.size()];
        for (double value : values) {
            boolean found = false;
            for (int i = 0; (!found) && (i < binStarts.size()); i++) {
                if ((binStarts.get(i) <= value) && (binEnds.get(i) >= value)) {
                    bincounts[i]++;
                    found = true;
                }
            }
            assert found : "Value "+value+" out of binning range "+binStarts.getFirst()+" to "+binEnds.getFirst();
        }
        // work out bin density
        double[] bindensity = new double[binStarts.size()];
        for (int i = 0; i < binStarts.size(); i++) {
            bindensity[i] = binStarts.get(i) / (binEnds.get(i) - binStarts.get(i));
        }
        // generate records
        List<BinContents> bins = new ArrayList<BinContents>();
        for (int i = 0; i < binStarts.size(); i++) {
            bins.add(new BinContents(binStarts.get(i), binEnds.get(i), bincounts[i], bindensity[i]));
        }
        return bins;
    }

}
