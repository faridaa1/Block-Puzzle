package blocks;

/**
 * Logical model for the Blocks Puzzle
 * This handles the game logic, such as the grid, the pieces, and the rules for
 * placing pieces and removing lines and subgrids.
 * <p>
 * Note this has no dependencies on the UI or the game view, and no
 * concept of pixel-space or screen coordinates.
 * <p>
 * The standard block puzzle is on a 9x9 grid, so all placeable shapes will have
 * cells in that range.
 */

import blocks.BlockShapes.*;

import java.util.*;

public class Model2dArray extends State2dArray implements ModelInterface {
    List<Shape> regions = new RegionHelper().allRegions();

    public Model2dArray() {
        grid = new boolean[width][height];
        // initially all cells are empty (false) - they would be by default anyway
        // but this makes it explicit
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j] = false;
            }
        }
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public boolean canPlace(Piece piece) {
        // interestingly, for canPlace we could also use sets to store the occupied cells and then check if the shape's cells intersect with the occupied cells
        // check if the shape can be placed at this loc
        return piece.cells().stream().noneMatch(cell -> cell.x() >= grid.length || cell.y() >= grid.length || cell.x() < 0 || cell.y() < 0 || getOccupiedCells().contains(cell));
    }

    @Override
    public void place(Piece piece) {
        List<Shape> poppableRegions = getPoppableRegions(piece);
        for (Cell cell : piece.cells()) {
            grid[cell.x()][cell.y()] = true;
        }
        for (Shape region : poppableRegions) {
            remove(region);
        }
        score += (int) (Math.pow(poppableRegions.size(), 2) * 10);
        streak = poppableRegions.isEmpty() ? 0 : ++streak;
    }

    @Override
    public void remove(Shape region) {
        for (Cell cell : region) {
            grid[cell.x()][cell.y()] = false;
        }
    }

    @Override
    public boolean isComplete(Shape region) {
        // check if the shape is complete, i.e. all cells are occupied
        return getOccupiedCells().containsAll(region);
    }

    public boolean wouldBeComplete(Shape region, List<Cell> toAdd) {
        // check if the shape is complete, i.e. all cells are occupied
        Set<Cell> tempOccupiedCells = new HashSet<>(getOccupiedCells());
        tempOccupiedCells.addAll(toAdd);
        return tempOccupiedCells.containsAll(region);
    }

    @Override
    public boolean isGameOver(List<Shape> palettePieces) {
        // if any shape in the palette can be placed, the game is not over
        for (Shape shape : palettePieces) {
            if (canPlaceAnywhere(shape)) return false;
        }
        return true;
    }

    public boolean canPlaceAnywhere(Shape shape) {
        // check if the shape can be placed anywhere on the grid
        // by checking if it can be placed at any loc
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                if (canPlace(new Piece(shape, new Cell(row, column)))) return true;
            }
        }
        return false;
    }

    @Override
    public List<Shape> getPoppableRegions(Piece piece) {
        // iterate over the regions
        List<Shape> poppableRegions = new ArrayList<>();
        if (!canPlace(piece)) return poppableRegions;
        return regions.stream().filter(region -> wouldBeComplete(region, piece.cells())).toList();
    }

    @Override
    public Set<Cell> getOccupiedCells() {
        Set<Cell> occupiedCells = new HashSet<>();
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                if (grid[row][column]) occupiedCells.add(new Cell(row, column));
            }
        }
        return occupiedCells;
    }
}
