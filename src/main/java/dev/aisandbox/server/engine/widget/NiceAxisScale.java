package dev.aisandbox.server.engine.widget;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Paul Heckbert's "Nice Numbers for Graph Labels" algorithm that
 * appears in the first Graphics Gems book.
 * <p>
 * Graphics Gems - Andrew Glassner
 * ISBN 978-0-08-050753-8
 */

public class NiceAxisScale {
    private static final int CONST = 5;

    private final int maxTicks = 5;
    private final double range;
    private final double tickSpacing;
    @Getter
    private final double minimum;
    @Getter
    private final double maximum;

    public NiceAxisScale(double minimumValue, double maximumValue) {
        // special case - min = max
        if (minimumValue==maximumValue) {
            minimumValue -= 0.5;
            maximumValue += 0.5;
        }
        range = nice(maximumValue - minimumValue, false);
        tickSpacing = nice(range / (maxTicks - 1), true);
        minimum = Math.floor(minimumValue / tickSpacing) * tickSpacing;
        maximum = Math.ceil(maximumValue / tickSpacing) * tickSpacing;
    }

    /**
     * Find a 'nice' number approximately equal to <i>value</i>.
     *
     * @param value the input value
     * @param round round the number down (true) or up (false)
     * @return a 'nice' number near <i>value</i>
     */
    protected static double nice(double value, boolean round) {
        double exponent; // exponent of range
        double fraction; // fractional part of range
        double niceFraction; // nice, rounded fraction

        exponent = Math.floor(Math.log10(value));
        fraction = value / Math.pow(10, exponent);

        if (round) {
            if (fraction < 1.5)
                niceFraction = 1;
            else if (fraction < 3)
                niceFraction = 2;
            else if (fraction < 7)
                niceFraction = 5;
            else
                niceFraction = 10;
        } else {
            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 5)
                niceFraction = 5;
            else
                niceFraction = 10;
        }
        return niceFraction * Math.pow(10, exponent);
    }


    public double getScaledValue(double value) {
        return (value - minimum) / (maximum - minimum);
    }

    public List<Double> getTicks() {
        List<Double> ticks = new ArrayList<Double>();
        double currentTick = minimum;
        while (currentTick <= maximum) {
            ticks.add(currentTick);
            currentTick += tickSpacing;
        }
        return ticks;
    }

}
