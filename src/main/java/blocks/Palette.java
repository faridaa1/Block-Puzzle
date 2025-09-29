package blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import blocks.BlockShapes.Shape;
import blocks.BlockShapes.ShapeSet;
import blocks.BlockShapes.SpriteState;
import blocks.BlockShapes.Sprite;
import blocks.BlockShapes.PixelLoc;

public class Palette {
    ArrayList<Shape> shapes = new ArrayList<>();
    List<Sprite> sprites;
    int nShapes = 3;

    public Palette() {
        shapes.addAll(new ShapeSet().getShapes());
        sprites = new ArrayList<>();
        replenish();
    }

    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    public ArrayList<Shape> getShapesToPlace() {
        // return a list of shapes that are in the palette - could use streams to filter this
        return sprites.stream().filter(s -> s.state == SpriteState.IN_PALETTE).map(s -> s.shape).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Sprite> getSprites() {
        return sprites;
    }


    public Sprite getSprite(PixelLoc mousePoint, int cellSize) {
        // if we have a sprite that contains the point (px, py), return it
        // and the size of the cells - the sprite location is already in pixel coordinates
        for (Sprite sprite : sprites) {
            if (sprite.contains(mousePoint, cellSize)
                    || sprite.shape.get(0).x() < sprite.shape.get(0).y()
                        && sprite.contains(new PixelLoc(mousePoint.x(), mousePoint.y() + sprite.shape.get(0).y()*cellSize), cellSize)) {
                    // accounting for shapes that don't start in the top left
                return sprite;
            }
        }
        return null;
    }

    private int nReadyPieces() {
        int count = 0;
        for (Sprite sprite : sprites) {
            if (sprite.state == SpriteState.IN_PALETTE || sprite.state == SpriteState.IN_PLAY) {
                count++;
            }
        }
        return count;
    }

    public void doLayout(int x0, int y0, int cellSize) {
        // layout the sprites in the palette
        int spriteNumber = 0;
        for (Sprite sprite : sprites) {
            if (sprite.state != SpriteState.IN_PLAY) {
                sprite.py = y0;
                sprite.px = x0 + (spriteNumber*cellSize*6);
            }
            if (spriteNumber == 2) {
                spriteNumber = 0;
                y0 += cellSize*5;
            } else {
                spriteNumber++;
            }
        }
    }

    public void replenish() {
        if (nReadyPieces() > 0) {
            return;
        }
        // clear the sprites and add new randomly selected shapes
        sprites.clear();

        for (int i = 0; i < nShapes; i++) {
            // Randomly determine sprites
            sprites.add(new Sprite(shapes.get(new Random().nextInt(shapes.size())), 0, 0));
        }
    }

    public static void main(String[] args) {
        Palette palette = new Palette();
        System.out.println(palette.shapes);
        System.out.println(palette.sprites);
        palette.doLayout(0, 0, 20);
        System.out.println(palette.sprites);
    }
}