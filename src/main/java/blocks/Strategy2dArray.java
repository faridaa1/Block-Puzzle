package blocks;

import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Cell;

import java.util.*;

public class Strategy2dArray implements ModelStrategy {

    Model2dArray model;

    public Strategy2dArray(Model2dArray model) {
        this.model = model;
    }

    @Override
    public boolean wouldPopRegion(Piece piece) {
        // Ensures priority is given to poppable position
        return model.regions.stream().anyMatch(shape -> model.wouldBeComplete(shape, piece.cells()));
    }

    @Override
    public Cell getValidStartingPoint(Shape shape) {
        List<Cell> validStartingPoints = new ArrayList<>();
        for (int row = 0; row < model.grid.length; row++) {
            for (int column = 0; column < model.grid[row].length; column++) {
                final Piece piece = new Piece(shape, new Cell(row, column));
                if (model.canPlace(piece)) {
                    if (wouldPopRegion(piece)) return new Cell(row, column);
                    validStartingPoints.add(new Cell(row, column));
                }
            }
        }
        if (validStartingPoints.isEmpty()) return null;
        return validStartingPoints.get(new Random().nextInt(validStartingPoints.size()));
    }

    @Override
    public int getStreak() {
        return model.streak;
    }
}
