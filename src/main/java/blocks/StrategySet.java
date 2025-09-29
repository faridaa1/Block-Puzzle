package blocks;

import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StrategySet implements ModelStrategy {
    ModelSet model;

    public StrategySet(ModelSet model) {
        this.model = model;
    }

    @Override
    public boolean wouldPopRegion(Piece piece) {
        // Ensures priority is given to poppable position
        return model.regions.stream().anyMatch(shape -> model.getPoppableRegions(piece).contains(shape));
    }

    @Override
    public Cell getValidStartingPoint(Shape shape) {
        List<Cell> validStartingPoints = new ArrayList<>();
        for (Cell cell : model.locations) {
            if (model.canPlace(new Piece(shape, cell))) {
                if (wouldPopRegion(new Piece(shape, cell))) return cell;
                validStartingPoints.add(cell);
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
