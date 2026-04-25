import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// --- Core Logic ---

// Renamed from Color to PieceColor to avoid naming conflicts with java.awt.Color
enum PieceColor { WHITE, BLACK }

abstract class Piece {
    int row, col;
    PieceColor color;
    boolean hasMoved = false;

    public Piece(int row, int col, PieceColor color) {
        this.row = row; this.col = col; this.color = color;
    }

    public abstract boolean isValidMove(int nR, int nC, Piece[][] board);

    protected boolean isPathClear(int nR, int nC, Piece[][] board) {
        int rDir = Integer.compare(nR, row);
        int cDir = Integer.compare(nC, col);
        int currR = row + rDir;
        int currC = col + cDir;
        while (currR != nR || currC != nC) {
            if (board[currR][currC] != null) return false;
            currR += rDir; currC += cDir;
        }
        return true;
    }
}

class Pawn extends Piece {
    public Pawn(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        int dir = (color == PieceColor.WHITE) ? -1 : 1;
        int startRow = (color == PieceColor.WHITE) ? 6 : 1;

        if (nC == col && board[nR][nC] == null) {
            if (nR == row + dir) return true;
            if (row == startRow && nR == row + 2 * dir && board[row + dir][col] == null) return true;
        }
        if (Math.abs(nC - col) == 1 && nR == row + dir && board[nR][nC] != null && board[nR][nC].color != color) {
            return true;
        }
        return false;
    }
}

class Rook extends Piece {
    public Rook(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        if (nR != row && nC != col) return false;
        if (!isPathClear(nR, nC, board)) return false;
        return board[nR][nC] == null || board[nR][nC].color != color;
    }
}

class Knight extends Piece {
    public Knight(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        int dR = Math.abs(nR - row);
        int dC = Math.abs(nC - col);
        return (dR * dC == 2) && (board[nR][nC] == null || board[nR][nC].color != color);
    }
}

class Bishop extends Piece {
    public Bishop(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        if (Math.abs(nR - row) != Math.abs(nC - col)) return false;
        if (!isPathClear(nR, nC, board)) return false;
        return board[nR][nC] == null || board[nR][nC].color != color;
    }
}

class Queen extends Piece {
    public Queen(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        if (nR != row && nC != col && Math.abs(nR - row) != Math.abs(nC - col)) return false;
        if (!isPathClear(nR, nC, board)) return false;
        return board[nR][nC] == null || board[nR][nC].color != color;
    }
}

class King extends Piece {
    public King(int r, int c, PieceColor color) { super(r, c, color); }
    @Override
    public boolean isValidMove(int nR, int nC, Piece[][] board) {
        int dR = Math.abs(nR - row);
        int dC = Math.abs(nC - col);
        if (dR <= 1 && dC <= 1) {
            return board[nR][nC] == null || board[nR][nC].color != color;
        }
        return false;
    }
}

// --- Board & GUI ---

public class ChessGame extends JFrame {
    private Piece[][] board = new Piece[8][8];
    private final JButton[][] buttons = new JButton[8][8];
    private PieceColor turn = PieceColor.WHITE;
    private Integer selR = null, selC = null;

    public ChessGame() {
        setupBoard();
        initUI();
    }

    private void setupBoard() {
        PieceColor[] colors = {PieceColor.BLACK, PieceColor.WHITE};
        for (PieceColor c : colors) {
            int row = (c == PieceColor.BLACK) ? 0 : 7;
            int pRow = (c == PieceColor.BLACK) ? 1 : 6;
            board[row][0] = new Rook(row, 0, c); board[row][7] = new Rook(row, 7, c);
            board[row][1] = new Knight(row, 1, c); board[row][6] = new Knight(row, 6, c);
            board[row][2] = new Bishop(row, 2, c); board[row][5] = new Bishop(row, 5, c);
            board[row][3] = new Queen(row, 3, c); board[row][4] = new King(row, 4, c);
            for (int i = 0; i < 8; i++) board[pRow][i] = new Pawn(pRow, i, c);
        }
    }

    private void initUI() {
        setTitle("Java Chess (2-Player)");
        setLayout(new GridLayout(8, 8));
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                buttons[r][c] = new JButton();
                buttons[r][c].setFont(new Font("Serif", Font.PLAIN, 45));
                buttons[r][c].setFocusPainted(false);
                // Using java.awt.Color now works perfectly
                buttons[r][c].setBackground((r + c) % 2 == 0 ? new Color(240, 217, 181) : new Color(181, 136, 99));
                int finalR = r, finalC = c;
                buttons[r][c].addActionListener(e -> onClick(finalR, finalC));
                add(buttons[r][c]);
            }
        }
        updateUI();
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onClick(int r, int c) {
        if (selR == null) {
            if (board[r][c] != null && board[r][c].color == turn) {
                selR = r; selC = c;
                buttons[r][c].setBackground(Color.YELLOW);
            }
        } else {
            Piece p = board[selR][selC];
            if (p.isValidMove(r, c, board)) {
                board[r][c] = p;
                board[selR][selC] = null;
                p.row = r; p.col = c; p.hasMoved = true;
                if (p instanceof Pawn && (r == 0 || r == 7)) board[r][c] = new Queen(r, c, p.color);
                turn = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            }
            buttons[selR][selC].setBackground((selR + selC) % 2 == 0 ? new Color(240, 217, 181) : new Color(181, 136, 99));
            selR = null; selC = null;
            updateUI();
        }
    }

    private void updateUI() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                buttons[r][c].setText(getSymbol(p));
            }
        }
    }

    private String getSymbol(Piece p) {
        if (p == null) return "";
        if (p instanceof King) return p.color == PieceColor.WHITE ? "♔" : "♚";
        if (p instanceof Queen) return p.color == PieceColor.WHITE ? "♕" : "♛";
        if (p instanceof Rook) return p.color == PieceColor.WHITE ? "♖" : "♜";
        if (p instanceof Bishop) return p.color == PieceColor.WHITE ? "♗" : "♝";
        if (p instanceof Knight) return p.color == PieceColor.WHITE ? "♘" : "♞";
        if (p instanceof Pawn) return p.color == PieceColor.WHITE ? "♙" : "♟";
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGame::new);
    }
}
