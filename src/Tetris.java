// Fichier: Tetris.java (Classe Principale)
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;

public class Tetris extends JFrame {

    private JLabel statusBar;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        // Création de la barre de statut pour le score et game over
        statusBar = new JLabel("0");
        statusBar.setBackground(Color.BLACK);
        statusBar.setForeground(Color.WHITE);
        statusBar.setOpaque(true);
        add(statusBar, BorderLayout.SOUTH);

        // Création du panneau de jeu (Board)
        Board board = new Board(this);
        add(board, BorderLayout.CENTER);
        board.start();

        setTitle("Tetris en Java");
        setSize(200, 440); // Augmenté un peu pour la barre de statut
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public JLabel getStatusBar() {
        return statusBar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}