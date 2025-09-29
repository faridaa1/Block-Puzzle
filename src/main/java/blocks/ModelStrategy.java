package blocks;

import blocks.BlockShapes.Cell;
import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Piece;

public interface ModelStrategy {
   Cell getValidStartingPoint(Shape shape);
   int getStreak();
   boolean wouldPopRegion(Piece piece);
}
