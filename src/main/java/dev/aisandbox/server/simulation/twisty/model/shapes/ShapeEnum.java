/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model.shapes;

import lombok.Getter;

/**
 * Enum representing different types of shapes used in the simulation. Each enum constant is
 * associated with a specific implementation of {@link CellShape}.
 */
public enum ShapeEnum {
  /**
   * Represents a square shape.
   */
  SQUARE(new Square());

  /**
   * The specific shape instance associated with the enum constant.
   */
  @Getter
  private final CellShape shape;

  /**
   * Constructor for the ShapeEnum.
   *
   * @param shape The specific {@link CellShape} instance associated with the enum constant.
   */
  ShapeEnum(CellShape shape) {
    this.shape = shape;
  }
}
