import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JPanel implements ActionListener {

    // Dimensions de la grille en nombre de cellules
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    // Taille d'un carré en pixels
    private final int SQUARE_SIZE = 20;

    // Constantes pour le score
    private final int SCORE_PER_LINE = 100; // Points par ligne
    private final int SCORE_TETRIS = 800;   // Bonus pour 4 lignes en même temps

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;

    private int numLinesRemoved = 0;
    private int currentScore = 0;
    private int curX = 0;
    private int curY = 0;
    private Shape currentPiece;
    private Tetrominoe[] board;

    private Tetris parent;

    public Board(Tetris parent) {
        this.parent = parent;
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        currentPiece = new Shape();
        timer = new Timer(400, this);

        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
        clearBoard();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    private int squareWidth() { return (int) getSize().getWidth() / BOARD_WIDTH; }
    private int squareHeight() { return (int) getSize().getHeight() / BOARD_HEIGHT; }

    private Tetrominoe shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    public void start() {
        if (isPaused) return;

        isStarted = true;
        isFallingFinished = false;
        isGameOver = false;
        numLinesRemoved = 0;
        currentScore = 0;
        clearBoard();

        updateScore(); // Mise à jour de l'affichage du score
        newPiece();
        timer.start();
    }

    private void pause() {
        if (!isStarted || isPaused) {
            return;
        }
        isPaused = true;
        timer.stop();
        parent.getStatusBar().setText("Pause");
        repaint();
    }

    private void resume() {
        if (!isStarted || !isPaused) {
            return;
        }
        isPaused = false;
        updateScore(); // Restaurer l'affichage du score
        timer.start();
        repaint();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        // Dessine les pièces déjà tombées
        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - 1 - i);
                if (shape != Tetrominoe.NoShape) {
                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }

        // Dessine la pièce en train de tomber
        if (currentPiece.getShape() != Tetrominoe.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + currentPiece.x(i);
                int y = curY - currentPiece.y(i);
                drawSquare(g, x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - 1 - y) * squareHeight(),
                        currentPiece.getShape());
            }
        }

        // Dessine la grille
        drawGrid(g, boardTop);

        // Affiche "Game Over" si le jeu est terminé
        if (isGameOver) {
            drawGameOver(g);
        }
    }

    // Affiche le message "Game Over"
    private void drawGameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 18);
        FontMetrics fm = g.getFontMetrics(small);

        g.setColor(new Color(200, 0, 0)); // Rouge foncé
        g.setFont(small);
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }

    private void drawGrid(Graphics g, int boardTop) {
        g.setColor(new Color(50, 50, 50));

        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(0, boardTop + i * squareHeight(),
                    BOARD_WIDTH * squareWidth(), boardTop + i * squareHeight());
        }
        for (int j = 0; j <= BOARD_WIDTH; j++) {
            g.drawLine(j * squareWidth(), boardTop,
                    j * squareWidth(), boardTop + BOARD_HEIGHT * squareHeight());
        }
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(currentPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(currentPiece, curX, curY - 1)) {
            pieceDropped();
        }
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; ++i) {
            int x = curX + currentPiece.x(i);
            int y = curY - currentPiece.y(i);
            board[y * BOARD_WIDTH + x] = currentPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void newPiece() {
        currentPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + currentPiece.minY();

        if (!tryMove(currentPiece, curX, curY)) {
            currentPiece.setShape(Tetrominoe.NoShape);
            timer.stop();
            isStarted = false;
            isGameOver = true;
            parent.getStatusBar().setText("Game Over: " + currentScore);
            repaint(); // Force le redessin pour afficher "Game Over"
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (shapeAt(x, y) != Tetrominoe.NoShape) {
                return false;
            }
        }

        currentPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;
                // Descend toutes les lignes au-dessus
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
                // La ligne du haut devient vide
                for (int j = 0; j < BOARD_WIDTH; ++j) {
                    board[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Tetrominoe.NoShape;
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;

            // Calcul du score
            if (numFullLines == 1) {
                currentScore += SCORE_PER_LINE;
            } else if (numFullLines == 2) {
                currentScore += SCORE_PER_LINE * 3; // Bonus pour 2 lignes
            } else if (numFullLines == 3) {
                currentScore += SCORE_PER_LINE * 5; // Bonus pour 3 lignes
            } else if (numFullLines == 4) {
                currentScore += SCORE_TETRIS; // Bonus pour Tetris (4 lignes)
            }

            updateScore();
            isFallingFinished = true;
            currentPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }

    // Met à jour l'affichage du score
    private void updateScore() {
        parent.getStatusBar().setText("Score: " + currentScore + " | Lignes: " + numLinesRemoved);
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {
        Color colors[] = {
                new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || currentPiece.getShape() == Tetrominoe.NoShape) {
                // Démarrer un nouveau jeu avec N si le jeu est terminé
                if (isGameOver && e.getKeyCode() == 'n' || e.getKeyCode() == 'N') {
                    start();
                }
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == 'p' || keycode == 'P') {
                if(isPaused) resume(); else pause();
                return;
            }

            if (isPaused) return;

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(currentPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(currentPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(currentPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(currentPiece.rotateLeft(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case 'd':
                case 'D':
                    oneLineDown();
                    break;
            }
        }
    }
}