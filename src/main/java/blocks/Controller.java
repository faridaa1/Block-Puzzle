package blocks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import blocks.BlockShapes.Sprite;
import blocks.BlockShapes.PixelLoc;
import blocks.BlockShapes.SpriteState;
import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Shape;
import blocks.BlockShapes.Cell;

public class Controller extends MouseAdapter {
    GameView view;
    ModelInterface model;
    ModelStrategy strategy;
    Palette palette;
    JFrame frame;
    JPanel panel = new JPanel();
    Sprite selectedSprite = null;
    Piece ghostShape = null;
    String title = "Blocks Puzzle";
    boolean gameOver = false;

    public Controller(GameView view, ModelInterface model, Palette palette, JFrame frame, ModelStrategy strategy) {
        this.view = view;
        this.model = model;
        this.palette = palette;
        this.frame = frame;
        this.strategy = strategy;
        frame.setTitle(title);
        // force palette to do a layout
        palette.doLayout(view.margin, view.margin + ModelInterface.height * view.cellSize, view.paletteCellSize);
    }

    public void mousePressed(MouseEvent e) {
        // just call the model to try a piece selection given
        // this coordinate, and any other details such as margin and cell size
        // implementation of this method is provided, but you need to make the other controller methods work
        // see todos below
        PixelLoc loc = new PixelLoc(e.getX(), e.getY());
        Sprite sprite = palette.getSprite(loc, view.paletteCellSize);
        if (sprite != null && sprite.state == SpriteState.IN_PALETTE) {
            selectedSprite = sprite;
            selectedSprite.state = SpriteState.IN_PLAY;
            frame.remove(panel);
            frame.revalidate();
            frame.repaint();
            view.repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (selectedSprite == null) return;
        selectedSprite.px = e.getX();
        selectedSprite.py = e.getY();
        ghostShape = null;
        for (Sprite sprite : palette.getSprites()) {
            Piece piece = sprite.snapToGrid(view.margin, view.cellSize);
            if (sprite.state == SpriteState.IN_PLAY && model.canPlace(piece)) {
                ghostShape = piece;
                break;
            }
        }
        view.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        if (selectedSprite != null && ghostShape == null) {
            selectedSprite.state = SpriteState.IN_PALETTE;
        } else if (selectedSprite != null && model.canPlace(ghostShape)) {
            model.place(ghostShape);
            selectedSprite.state = SpriteState.PLACED;
        }
        selectedSprite=null;
        ghostShape=null;

        palette.replenish();
        palette.doLayout(view.margin, view.margin + ModelInterface.height * view.cellSize, view.paletteCellSize);

        if (isGameOver()) return;

        // update the title with the score and whether the game is over
        frame.setTitle(getTitle());
        view.repaint();
    }

    private String getTitle() {
        // make the title from the base title, score, and add GameOver if the game is over
        String title = this.title + " Score: " + model.getScore() + " Streak: " + strategy.getStreak();
        if (gameOver) {
            title += " Game Over!";
        }
        return title;
    }

    private boolean isGameOver() {
        List<Shape> shapesInPalette = palette.getSprites().stream()
                .filter(sprite -> sprite.state == SpriteState.IN_PALETTE)
                .map(sprite -> sprite.shape)
                .toList();

        if (model.isGameOver(shapesInPalette)) {
            gameOver = true;
        }
        frame.setTitle(getTitle());
        view.repaint();
        return gameOver;
    }

    private void nextSprite(int spriteIndex) {
        int newSpriteIndex = (spriteIndex+1)%palette.nShapes;
        randomBot(newSpriteIndex);
    }

    private void randomBot(int spriteIndex) {
        selectedSprite = palette.getSprites().get(spriteIndex);
        if (selectedSprite.state == SpriteState.PLACED) {
            gameOver = true;
            frame.setTitle(getTitle());
            view.repaint();
            return;
        }

        Cell startingPoint = strategy.getValidStartingPoint(selectedSprite.shape);
        if (startingPoint == null) {
            nextSprite(spriteIndex);
        }

        selectedSprite.state = SpriteState.IN_PLAY;
        new Bot().positionPiece(selectedSprite, startingPoint, view.margin, view.cellSize, () -> view.repaint(), (piece) -> {
            model.place(piece);
            selectedSprite.state = SpriteState.PLACED;
            palette.replenish();
            palette.doLayout(view.margin, view.margin + ModelInterface.height * view.cellSize, view.paletteCellSize);
            view.repaint();
            if (isGameOver()) return;
            nextSprite(spriteIndex);
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ModelInterface model = new ModelSet();
        ModelStrategy strategy = new StrategySet((ModelSet) model);
//        ModelInterface model = new Model2dArray();
//        ModelStrategy strategy = new Strategy2dArray((Model2dArray) model);
        Palette palette = new Palette();
        GameView view = new GameView(model, palette);
        Controller controller = new Controller(view, model, palette, frame, strategy);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        frame.setLayout(new BorderLayout());
        JButton randomButton = new JButton("Random Play Mode");
        randomButton.addActionListener(actionEvent -> {
            // identify playable position
            // check if sprite can be placed
            controller.randomBot(0);
            controller.panel.remove(randomButton);
            view.removeMouseListener(controller);
            view.removeMouseMotionListener(controller);
            controller.panel.revalidate();
            controller.panel.repaint();
        });
        controller.panel.add(randomButton);
        frame.add(controller.panel, BorderLayout.SOUTH);
        frame.add(view, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}