/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import dev.aisandbox.server.simulation.twisty.model.shapes.ShapeEnum;
import java.awt.Polygon;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a cell within the twisty simulation model. A cell contains information about its
 * shape, size, position, and color. It is used to represent individual elements in the twisty
 * puzzle simulation.
 */
public class Cell {

  /**
   * The shape of the cell, defined as a ShapeEnum type.
   */
  @Getter
  @Setter
  ShapeEnum shape;

  /**
   * The scale factor that determines the size of the cell.
   */
  @Getter
  @Setter
  int scale;

  /**
   * The x-coordinate of the cell's position.
   */
  @Getter
  @Setter
  int locationX;

  /**
   * The y-coordinate of the cell's position.
   */
  @Getter
  @Setter
  int locationY;

  /**
   * The color of the cell, defined as a ColourEnum type.
   */
  @Getter
  @Setter
  ColourEnum colour;

  /**
   * Gets the polygon representation of this cell. The polygon is positioned at the cell's location
   * coordinates and sized according to its scale.
   *
   * @return The Polygon representation of the cell, or null if no shape is defined
   */
  public Polygon getPolygon() {
    if (shape == null) {
      return null;
    } else {
      return shape.getShape().getPolygon(locationX, locationY, scale);
    }
  }

  /**
   * Returns a string representation of this Cell. Includes information about the cell's shape,
   * scale, and location.
   *
   * @return A string representation of the Cell
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("shape", shape)
        .append("scale", scale).append("locationX", locationX).append("locationY", locationY)
        .toString();
  }
}
