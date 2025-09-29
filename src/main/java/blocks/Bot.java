package blocks;

import blocks.BlockShapes.Cell;
import blocks.BlockShapes.Piece;
import blocks.BlockShapes.Sprite;

import javax.swing.*;

public class Bot {
    Timer timer;

    // Callback interface to notify when the bot is done moving
    public interface BotMovementCompleteCallback {
        void placePiece(Piece piece);
    }

    public void positionPiece(Sprite sprite, Cell startingPoint, int margin, int cellSize, Runnable repaint, BotMovementCompleteCallback callback) {
        if (startingPoint == null || sprite == null) return;
        Piece targetPiece = new Piece(sprite.shape, startingPoint);
        timer = new Timer(0, actionEvent -> {
            Piece currentPiece = sprite.snapToGrid(margin, cellSize);
            int targetX = targetPiece.cells().get(0).x();
            int currentX = currentPiece.cells().get(0).x();
            if (currentX != targetX) {
                sprite.px += (currentX < targetX) ? 2 : -2;
            } else if (currentPiece.cells().get(0).y() > targetPiece.cells().get(0).y()) {
                sprite.py -= 2;
            } else {
                timer.stop();
                callback.placePiece(currentPiece);  // Notify the callback that movement is done
                return;
            }
            repaint.run();
        });
        timer.start();
    }
}
