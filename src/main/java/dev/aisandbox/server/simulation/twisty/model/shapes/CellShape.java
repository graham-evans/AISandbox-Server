package dev.aisandbox.server.simulation.twisty.model.shapes;

import java.awt.Polygon;

/**
 * Interface defining the behavior of shapes used in the twisty simulation.
 * Implementations of this interface represent different geometric shapes
 * that can be used for cells in the simulation grid.
 */
public interface CellShape {

  /**
   * Creates a polygon representation of the shape centered at the specified location.
   * 
   * @param locationX The x-coordinate of the center of the shape
   * @param locationY The y-coordinate of the center of the shape
   * @param scale The scaling factor determining the size of the shape
   * @return A polygon object representing the shape with the specified parameters
   */
  Polygon getPolygon(int locationX, int locationY, int scale);
}
