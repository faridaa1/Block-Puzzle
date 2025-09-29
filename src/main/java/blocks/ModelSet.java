package blocks;

import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Cell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelSet extends StateSet implements ModelInterface {

    Set<Cell> locations = new HashSet<>();
    List<Shape> regions = new RegionHelper().allRegions();

    // we need a constructor to initialise the regions
    public ModelSet() {
        super();
        initialiseLocations();
    }

    @Override
    public int getScore() {
        return score;
    }

    private void initialiseLocations() {
        // having all grid locations in a set is in line with the set based approach
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                locations.add(new Cell(i, j));
            }
        }
    }

    @Override
    public boolean canPlace(Piece piece) {
        // can be placed if the cells are not occupied i.e. not in the occupiedCells set
        // though each one must be within the bounds of the grid
        for (Cell c : piece.cells()) {
            if (occupiedCells.contains(c)) {
                return false;
            }
        }
        return locations.containsAll(piece.cells());
    }

    @Override
    public void place(Piece piece) {
        // add the cells in the Piece to the occupiedCells set
        // then remove all the poppable regions
        // increment the score as function of the regions popped
        List<Shape> poppableRegions = getPoppableRegions(piece);
        occupiedCells.addAll(piece.cells());
        for (Shape region : poppableRegions) {
            remove(region);
        }
        score += (int) (Math.pow(poppableRegions.size(), 2) * 10);
        streak = poppableRegions.isEmpty() ? 0 : ++streak;
    }

    @Override
    public void remove(Shape region) {
        // remove the cells from the occupiedCells set
        occupiedCells.removeAll(region);
    }

    @Override
    public boolean isComplete(Shape region) {
        // use a stream to check if all the cells in the region are occupied
        return occupiedCells.containsAll(region);
    }

    @Override
    public boolean isGameOver(List<Shape> palettePieces) {
        // if any shape in the palette can be placed, the game is not over
        // use a helper function to check whether an individual shape can be placed anywhere
        for (Shape s : palettePieces) {
            if (canPlaceAnywhere(s)) return false;
        }
        return true;
    }

    public boolean canPlaceAnywhere(Shape shape) {
        // check if the shape can be placed anywhere on the grid
        // by checking if it can be placed at any loc
        for (Cell c : locations) {
            if (canPlace(new Piece(shape, c))) {
                return true;}
        }
        return false;
    }

    @Override
    public List<Shape> getPoppableRegions(Piece piece) {
        // return the regions that would be popped if the piece is placed
        // to do this we need to iterate over the regions and check if the piece overlaps enough to complete it
        // i.e. we can make a new set of occupied cells and check if the region is complete
        // if it is complete, we add it to the list of regions to be popped
        ArrayList<Shape> poppableRegions = new ArrayList<>();
        if (!canPlace(piece)) return poppableRegions;
        Set<Cell> tempOccupiedCells = new HashSet<>(occupiedCells);
        tempOccupiedCells.addAll(piece.cells());
        return regions.stream().filter(tempOccupiedCells::containsAll).toList();
    }

    @Override
    public Set<Cell> getOccupiedCells() {
        return occupiedCells;
    }
}