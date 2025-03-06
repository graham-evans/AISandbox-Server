package dev.aisandbox.server.simulation.twisty.model.shapes;

import lombok.Getter;

public enum ShapeEnum {
  SQUARE(new Square()),
  EQ_TRIANGLE(new EquilateralTriangle());

  @Getter private final CellShape shape;

  ShapeEnum(CellShape shape) {
    this.shape = shape;
  }
}
