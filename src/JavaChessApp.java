import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// --- MAIN ENTRY POINT ---
public class JavaChessApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java OOP Chess");
            GameController controller = new GameController();
            frame.add(controller.getView());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

// --- CORE MODELS ---
enum PieceColor { WHITE, BLACK }

abstract class Piece {
    protected PieceColor color;
    protected int row, col;
    protected boolean hasMoved = false;

    public Piece(PieceColor color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public abstract boolean isValidMove(int targetRow, int targetCol, Piece[][] grid);
    public abstract String getSymbol(); // Using Unicode characters for simplicity

    public PieceColor getColor() { return color; }
    public void setPosition(int r, int c) { this.row = r; this.col = c; this.hasMoved = true; }
}

class Pawn extends Piece {
    public Pawn(PieceColor color, int r, int c) { super(color, r, c); }
    
    @Override
    public boolean isValidMove(int tr, int tc, Piece[][] grid) {
        int dir = (color == PieceColor.WHITE) ? -1 : 1;
        int diffR = tr - row;
        int diffC = Math.abs(tc - col);

        // Standard forward move
        if (diffC == 0 && grid[tr][tc] == null) {
            if (diffR == dir) return true;
            if (!hasMoved && diffR == 2 * dir && grid[row + dir][col] == null) return true;
        }
        // Capture
        if (diffC == 1 && diffR == dir && grid[tr][tc] != null) {
            return grid[tr][tc].getColor() != color;
        }
        return false;
    }

    @Override public String getSymbol() { return color == PieceColor.WHITE ? "♙" : "♟"; }
}

class King extends Piece {
    public King(PieceColor color, int r, int c) { super(color, r, c); }
    @Override
    public boolean isValidMove(int tr, int tc, Piece[][] grid) {
        return Math.abs(tr - row) <= 1 && Math.abs(tc - col) <= 1;
    }
    @Override public String getSymbol() { return color == PieceColor.WHITE ? "♔" : "♚"; }
}

// Note: Add Rook, Bishop, Knight, Queen using similar logic...

class Board {
    private Piece[][] grid = new Piece[8][8];

    public Board() {
        initialize();
    }

    private void initialize() {
        // Simple setup for demonstration
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn(PieceColor.BLACK, 1, i);
            grid[6][i] = new Pawn(PieceColor.WHITE, 6, i);
        }
        grid[0][4] = new King(PieceColor.BLACK, 0, 4);
        grid[7][4] = new King(PieceColor.WHITE, 7, 4);
        // Fill in other pieces here...
    }

    public Piece getPiece(int r, int c) { return grid[r][c]; }
    
    public void movePiece(int r1, int c1, int r2, int c2) {
        Piece p = grid[r1][c1];
        grid[r2][c2] = p;
        grid[r1][c1] = null;
        if (p != null) p.setPosition(r2, c2);
    }

    public Piece[][] getGrid() { return grid; }
}

// --- CONTROLLER AND GUI ---
class GameController {
    private Board board;
    private GameView view;
    private PieceColor turn = PieceColor.WHITE;
    private int selRow = -1, selCol = -1;

    public GameController() {
        this.board = new Board();
        this.view = new GameView(this);
    }

    public void handleSquareClick(int r, int c) {
        if (selRow == -1) {
            Piece p = board.getPiece(r, c);
            if (p != null && p.getColor() == turn) {
                selRow = r; selCol = c;
            }
        } else {
            Piece p = board.getPiece(selRow, selCol);
            if (p != null && p.isValidMove(r, c, board.getGrid())) {
                board.movePiece(selRow, selCol, r, c);
                turn = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            }
            selRow = -1; selCol = -1;
        }
        view.updateBoard();
    }

    public Board getBoard() { return board; }
    public GameView getView() { return view; }
}

class GameView extends JPanel {
    private GameController controller;
    private JButton[][] squares = new JButton[8][8];

    public GameView(GameController controller) {
        this.controller = controller;
        setLayout(new GridLayout(8, 8));
        setPreferredSize(new Dimension(600, 600));

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c] = new JButton();
                squares[r][c].setFont(new Font("Serif", Font.BOLD, 40));
                final int row = r, col = c;
                squares[r][c].addActionListener(e -> controller.handleSquareClick(row, col));
                add(squares[r][c]);
            }
        }
        updateBoard();
    }

    public void updateBoard() {
        Board b = controller.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = b.getPiece(r, c);
                squares[r][c].setText(p != null ? p.getSymbol() : "");
                squares[r][c].setBackground((r + c) % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
            }
        }
    }
}
