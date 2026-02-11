/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;


import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * A JavaFX Region that wraps an ImageView and scales it to fill the available space.
 */
public class WrappedImageView extends Region {

  private final ImageView imageView;

  /**
   * Creates a WrappedImageView containing the specified ImageView.
   *
   * @param imageView the ImageView to wrap
   */
  public WrappedImageView(ImageView imageView) {
    this.imageView = imageView;
    getChildren().add(imageView);
  }

  @Override
  protected void layoutChildren() {
    // usable width and height:
    double width = getWidth() - snappedLeftInset() - snappedRightInset();
    double height = getHeight() - snappedTopInset() - snappedBottomInset();
    imageView.setFitWidth(Math.max(1, width));
    imageView.setFitHeight(Math.max(1, height));
    double imageWidth = imageView.getBoundsInLocal().getWidth();
    double imageHeight = imageView.getBoundsInLocal().getHeight();
    // center image (can also make this more complex and support alignment):
    double x = snappedLeftInset() + (width - imageWidth) / 2;
    double y = snappedTopInset() + (height - imageHeight) / 2;
    imageView.relocate(x, y);
  }


}
