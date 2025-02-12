package dev.aisandbox.server.engine.widget;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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
        ticks = niceAxisScale.getTicks().stream().filter(t -> (minimum <= t && t <= maximum)).collect(Collectors.toList());
    }

    @Override
    public double getScaledValue(double value) {
        return (value - minimum) / (maximum - minimum);
    }

}
