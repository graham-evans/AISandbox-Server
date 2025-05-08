package dev.aisandbox.server.simulation.twisty.model.shapes;

import java.awt.Polygon;

/**
 * Implementation of {@link CellShape} representing a unit square.
 * This class creates a square shape centered at a given location with the specified scale.
 * The square is implemented as a polygon with four vertices, ignoring rotation.
 */
public class Square implements CellShape {

  /**
   * Creates a polygon representing a square centered at the specified location.
   * 
   * @param locationX The x-coordinate of the center of the square
   * @param locationY The y-coordinate of the center of the square
   * @param scale The scaling factor determining the size of the square
   *             (distance from center to edge = scale)
   * @return A polygon object representing the square
   */
  @Override
  public Polygon getPolygon(int locationX, int locationY, int scale) {
    Polygon poly = new Polygon();
    // add points in counter-clockwise order
    poly.addPoint(locationX-scale, locationY-scale); // top-left vertex
    poly.addPoint(locationX+scale, locationY-scale); // top-right vertex
    poly.addPoint(locationX+scale, locationY+scale); // bottom-right vertex
    poly.addPoint(locationX-scale, locationY+scale); // bottom-left vertex
    return poly;
  }
}
