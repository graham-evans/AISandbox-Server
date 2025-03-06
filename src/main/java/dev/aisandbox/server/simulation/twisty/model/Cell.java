package dev.aisandbox.server.simulation.twisty.model;

import dev.aisandbox.server.simulation.twisty.model.shapes.ShapeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.awt.*;

public class Cell {

    @Getter
    @Setter
    ShapeEnum shape;
    @Getter
    @Setter
    int scale;
    @Getter
    @Setter
    int locationX;
    @Getter
    @Setter
    int locationY;
    @Getter
    @Setter
    int rotation;
    @Getter
    @Setter
    ColourEnum colour;

    public Polygon getPolygon() {
        if (shape == null) {
            return null;
        } else {
            return shape.getShape().getPolygon(locationX, locationY, rotation, scale);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("shape", shape)
                .append("scale", scale)
                .append("locationX", locationX)
                .append("locationY", locationY)
                .append("rotation", rotation)
                .toString();
    }
}
