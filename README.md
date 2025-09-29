# Blocks Puzzle
A game where pieces are placed onto a grid. Similar to Tetris and Sudoko, filling rows, columns, or mini-squares clears them, earning points.

## How to run
1. `cd src/main/java`
2. `javac blocks/Controller.java`
3. `java blocks/Controller`

## Features

### Sprite Drag and Drop 
Users select a sprite, which is expanded to indicate that it is in play, with a ghost shape shown over legal grid locations. 
Mouse release places the sprite in a legal location, or returns the sprite to its original palette position on an illegal position.

### Scoring
Each time a piece is placed, the score is updated.

### Score Streak
The streak increases for successive region pops.

### Game Over Detection
Game over is detected by determining whether a sprite can be placed on any available location.

### Random Play Mode
Mode where user can watch a random bot play the game.