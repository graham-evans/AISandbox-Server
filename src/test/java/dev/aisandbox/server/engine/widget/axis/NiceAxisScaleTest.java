package dev.aisandbox.server.engine.widget.axis;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NiceAxisScaleTest {

    @Test
    public void testNiceNumber() {
        assertEquals(2.0, NiceAxisScale.nice(2.0002,true),0.00001);
        assertEquals(2.0,NiceAxisScale.nice(1.9998,false),0.00001);
    }

    @Test
    public void testAxis() {
        NiceAxisScale scale = new NiceAxisScale(0.002, 9.84);
        // check scale is nice
        assertEquals(0.0,scale.getMinimum(),0.00001);
        assertEquals(10.0,scale.getMaximum(),0.00001);
        // check scale works
        assertEquals(0.2,scale.getScaledValue(2.0),0.00001);
    }

    @Test
    public void testSmallAxis() {
        NiceAxisScale scale = new NiceAxisScale(4.5, 5.5);
        // check scale is nice
        assertEquals(4.4,scale.getMinimum(),0.00001);
        assertEquals(5.6,scale.getMaximum(),0.00001);
        // check scale works
        assertEquals(0.5,scale.getScaledValue(5.000),0.00001);
    }

    @Test
    public void testZeroAxis() {
        NiceAxisScale scale = new NiceAxisScale(5.000, 5.000);
        // check scale is nice
        assertEquals(4.4,scale.getMinimum(),0.00001);
        assertEquals(5.6,scale.getMaximum(),0.00001);
        // check scale works
        assertEquals(0.5,scale.getScaledValue(5.000),0.00001);
    }

    @Test
    public void testTicks() {
        NiceAxisScale scale = new NiceAxisScale(0.000, 10.000);
        // check scale is nice
        assertEquals(0.0,scale.getMinimum(),0.00001);
        assertEquals(10.0,scale.getMaximum(),0.00001);
        // check scale works
        List<Double> expected = List.of(0.0,2.0,4.0,6.0,8.0,10.0);
        List<Double> actual = scale.getTicks();
        assertEquals(expected,actual);
    }

}