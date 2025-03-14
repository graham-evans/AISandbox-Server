package dev.aisandbox.server.engine.maths.bins;

import dev.aisandbox.server.engine.maths.BinContents;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binner which creates (b-a+1) bins between a and b.
 */
@RequiredArgsConstructor
public class IntegerBinner implements BinningEngine{

    private final int minValue;
    private final int maxValue;

    @Override
    public List<BinContents> binValues(List<Double> values) {
        // count the frequency of values
        Map<Integer,Integer> binmap = new HashMap<>();
        values.forEach(value -> {
            // convert to integer
            int vInt =  value.intValue();
            binmap.computeIfPresent(vInt, (k, v) -> v + 1);
            binmap.putIfAbsent(vInt,1);
        });
        // create bins
        List<BinContents> bins = new ArrayList<>();
        for(int n=minValue;n<=maxValue;n++){
            int count = binmap.getOrDefault(n,1);
            bins.add(new BinContents(n-0.5,n+0.5,count,(double)count / (double)values.size()));
        }
        return bins;
    }
}
