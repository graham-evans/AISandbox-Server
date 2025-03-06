package dev.aisandbox.server.simulation.twisty.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

public class CompiledMove {

    private final int cardinality;

    private final int[] matrix;

    @Getter
    @Setter
    BufferedImage image;
    @Getter
    @Setter
    int cost;

    public CompiledMove(int cardinality) {
        this.cardinality = cardinality;
        matrix = new int[cardinality];
    }

    protected void resetMove() {
        for (int i = 0; i < cardinality; i++) {
            matrix[i] = i;
        }
    }

    protected void setMatrixElement(int index, int value) {
        matrix[index] = value;
    }

    protected int getMatrixElement(int index) {
        return matrix[index];
    }

    public String applyMove(String state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cardinality; i++) {
            sb.append(state.charAt(matrix[i]));
        }
        return sb.toString();
    }
}
