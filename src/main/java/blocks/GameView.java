package blocks;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Cell;
import blocks.BlockShapes.ShapeSet;
import blocks.BlockShapes.SpriteState;
import blocks.BlockShapes.Sprite;

// class should work in a basic way as provided if all the todos are implemented in the other classes
// though you need to provide or complete the implementations for the methods in todos below


public class GameView extends JComponent {
    ModelInterface model;
    Palette palette;
    int margin = 5;
    int shapeRegionHeight;
    int cellSize = 40;
    int paletteCellSize = 20;
    int shrinkSize = 30;
    Piece ghostShape = null;
    List<Shape> poppableRegions = null;

    public GameView(ModelInterface model, Palette palette) {
        this.model = model;
        this.palette = palette;
        this.shapeRegionHeight = cellSize * ModelInterface.height / 2;
    }

    private void paintShapePalette(Graphics g, int cellSize) {
        // paint a background colour
        // then get the list of current shapes from the palette
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(margin, margin + ModelInterface.height * cellSize, ModelInterface.width * cellSize, shapeRegionHeight);

        for (Sprite sprite : palette.getSprites().stream().filter(sprite -> sprite.state != SpriteState.PLACED).toList()) {
            int x = sprite.px;
            int y = sprite.py;
            Shape shape = sprite.shape;

            for (int cell=0; cell < shape.size(); cell++) {
                Cell cellShape = shape.get(cell);
                g.setColor(Color.blue);
                int size = sprite.state == SpriteState.IN_PALETTE ? paletteCellSize : shrinkSize;
                int cellX = sprite.state == SpriteState.IN_PALETTE ? x : cellShape.x()*(cellSize/4)+x;
                int cellY = cellShape.y()*(sprite.state == SpriteState.IN_PALETTE ? size : cellSize);
                g.fillRect(cellX, cellY+y, size, size);
                g.setColor(sprite.state == SpriteState.IN_PALETTE ? Color.blue.darker() : Color.blue); // cell border
                g.drawRect(cellX, cellY+y, size, size);
                if (cell + 1 < shape.size()) {
                    x += (shape.get(cell + 1).x() - cellShape.x()) * size;
                }
            }
        }
    }

    private void paintPoppableRegions(Graphics g, int cellSize) {
        if (ghostShape == null) return;
        poppableRegions = model.getPoppableRegions(ghostShape);
        if (poppableRegions.isEmpty()) return;
        g.setColor(new Color(0, 0, 0, 75)); // black with 50% transparency
        for (int x = 0; x < ModelInterface.width; x++) {
            for (int y = 0; y < ModelInterface.height; y++) {
                if (cellInPoppableRegion(new Cell(x, y))) {
                    g.fill3DRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize, true);
                }
            }
        }
    }

    private boolean cellInPoppableRegion(Cell cell) {
        return poppableRegions != null && poppableRegions.stream().anyMatch(shape -> shape.contains(cell));
    }

    private Piece getGhostShape() {
        for (Sprite sprite : palette.getSprites()) {
            if (sprite.state == SpriteState.IN_PLAY) {
                return sprite.snapToGrid(margin, cellSize);
            }
        }
        return null;
    }

    private void paintGhostCell(Graphics g, int cellSize, int x, int y) {
        boolean isPoppableCell = cellInPoppableRegion(new Cell(x,y));
        g.setColor(isPoppableCell ? new Color(0, 0, 0, 75) : new Color(0, 255, 255, 128));
        if (isPoppableCell) {
            g.fill3DRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize, true);
        } else {
            g.fillRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize);
        }
    }

    private void paintGhostShape(Graphics g, int cellSize) {
        ghostShape = getGhostShape();
        if (ghostShape == null || !model.canPlace(ghostShape)) return;
        g.setColor(new Color(0, 255, 255, 128)); // Cyan with 50% transparency
        for (Cell cell : ghostShape.cells()) {
            paintGhostCell(g, cellSize, cell.x(), cell.y());
        }
    }

    private void paintGrid(Graphics g) {
        int x0 = margin;
        int y0 = margin;
        int width = ModelInterface.width * cellSize;
        int height = ModelInterface.height * cellSize;
        Set<Cell> occupiedCells = model.getOccupiedCells();
        g.setColor(Color.BLACK);
        g.drawRect(x0, y0, width, height);
        for (int x = 0; x < ModelInterface.width; x++) {
            for (int y = 0; y < ModelInterface.height; y++) {
                g.setColor(occupiedCells.contains(new Cell(x, y)) ? Color.green : Color.white);
                g.fill3DRect(x0 + x * cellSize, y0 + y * cellSize, cellSize, cellSize, true);
            }
        }
    }

    private void paintMiniGrids(Graphics2D g) {
        // for now, we're going to do this based on the cellSize multiple
        int s = ModelInterface.subSize;
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);
        for (int x = 0; x < ModelInterface.width; x += s) {
            for (int y = 0; y < ModelInterface.height; y += s) {
                g.drawRect(margin + x * cellSize, margin + y * cellSize, s * cellSize, s * cellSize);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintGrid(g);
        paintMiniGrids((Graphics2D) g); // cosmetic
        paintGhostShape(g, cellSize);
        paintPoppableRegions(g, cellSize);
        paintShapePalette(g, cellSize);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                ModelInterface.width * cellSize + 2 * margin,
                ModelInterface.height * cellSize + 2 * margin + shapeRegionHeight
        );
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Clean Blocks");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ModelInterface model = new ModelSet();
        Shape shape = new ShapeSet().getShapes().get(0);
        Piece piece = new Piece(shape, new Cell(0, 0));
        Palette palette = new Palette();
        model.place(piece);
        frame.add(new GameView(model, palette));
        frame.pack();
        frame.setVisible(true);
    }
}