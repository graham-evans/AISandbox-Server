/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.twisty.model;

import java.awt.image.BufferedImage;

/**
 * A record representing the result of a twisty puzzle move.
 *
 * @param cost the cost associated with this move
 * @param icon the visual icon representing this move
 */
public record MoveResult(int cost, BufferedImage icon) {

}
