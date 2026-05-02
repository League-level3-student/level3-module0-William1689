import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChessGame extends JFrame {
    public ChessGame() {
        Object[] options = {"Play Human", "Play Defensive Bot"};
        int n = JOptionPane.showOptionDialog(null, 
            "Select Game Mode\n(Bot uses Alpha-Beta Pruning to avoid blunders)", 
            "Java Chess AI v3",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options);
        
        setTitle("Chess AI - Deep Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new ChessBoard(n == 1));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { new ChessGame(); }
        });
    }
}

class ChessBoard extends JPanel {
    private final int TILE_SIZE = 80;
    private String[][] board = new String[8][8];
    private int selR = -1, selC = -1;
    private boolean whiteTurn = true, vsBot;
    private boolean wKMoved, wR1Moved, wR2Moved, bKMoved, bR1Moved, bR2Moved;

    public ChessBoard(boolean vsBot) {
        this.vsBot = vsBot;
        setPreferredSize(new Dimension(TILE_SIZE * 8, TILE_SIZE * 8));
        setupBoard();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (vsBot && !whiteTurn) return;
                handleInput(e.getY() / TILE_SIZE, e.getX() / TILE_SIZE);
            }
        });
    }

    private void setupBoard() {
        String[] back = {"R", "N", "B", "Q", "K", "B", "N", "R"};
        for (int i = 0; i < 8; i++) {
            board[0][i] = back[i]; board[1][i] = "P";
            board[6][i] = "p"; board[7][i] = back[i].toLowerCase();
        }
    }

    private void handleInput(int r, int c) {
        if (r < 0 || r > 7 || c < 0 || c > 7) return;
        if (selR == -1) {
            if (board[r][c] != null && isWhite(board[r][c]) == whiteTurn) {
                selR = r; selC = c;
            }
        } else {
            if (isValidMove(selR, selC, r, c, true)) {
                executeMove(selR, selC, r, c);
                whiteTurn = !whiteTurn;
                repaint();
                if (!checkGameState() && vsBot && !whiteTurn) {
                    Timer t = new Timer(500, new ActionListener() {
                        @Override public void actionPerformed(ActionEvent e) {
                            botMove();
                            ((Timer)e.getSource()).stop();
                        }
                    });
                    t.start();
                }
            }
            selR = -1; selC = -1;
        }
        repaint();
    }

    private void botMove() {
        // Search 3 plies deep (Self -> Human -> Self)
        Move best = alphaBetaSearch(3, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
        if (best != null) {
            executeMove(best.r1, best.c1, best.r2, best.c2);
            whiteTurn = true;
            checkGameState();
            repaint();
        }
    }

    // --- ALPHA-BETA PRUNING ENGINE ---
    private Move alphaBetaSearch(int depth, int alpha, int beta, boolean isMaximizing) {
        List<Move> moves = getAllLegalMoves(isMaximizing);
        Move bestMove = null;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : moves) {
                String target = board[m.r2][m.c2];
                String source = board[m.r1][m.c1];
                board[m.r2][m.c2] = source; board[m.r1][m.c1] = null;
                int eval = minimax(depth - 1, alpha, beta, false);
                board[m.r1][m.c1] = source; board[m.r2][m.c2] = target;

                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = m;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return bestMove;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move m : moves) {
                String target = board[m.r2][m.c2];
                String source = board[m.r1][m.c1];
                board[m.r2][m.c2] = source; board[m.r1][m.c1] = null;
                int eval = minimax(depth - 1, alpha, beta, true);
                board[m.r1][m.c1] = source; board[m.r2][m.c2] = target;

                if (eval < minEval) {
                    minEval = eval;
                    bestMove = m;
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return bestMove;
        }
    }

    private int minimax(int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) return evaluateBoard();

        List<Move> moves = getAllLegalMoves(isMaximizing);
        if (moves.isEmpty()) return evaluateBoard();

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : moves) {
                String target = board[m.r2][m.c2];
                String source = board[m.r1][m.c1];
                board[m.r2][m.c2] = source; board[m.r1][m.c1] = null;
                int eval = minimax(depth - 1, alpha, beta, false);
                board[m.r1][m.c1] = source; board[m.r2][m.c2] = target;
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move m : moves) {
                String target = board[m.r2][m.c2];
                String source = board[m.r1][m.c1];
                board[m.r2][m.c2] = source; board[m.r1][m.c1] = null;
                int eval = minimax(depth - 1, alpha, beta, true);
                board[m.r1][m.c1] = source; board[m.r2][m.c2] = target;
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluateBoard() {
        int total = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    int val = getPieceValue(board[r][c]);
                    // Basic center bonus to encourage development
                    if (r >= 2 && r <= 5 && c >= 2 && c <= 5) val += 10; 
                    total += isWhite(board[r][c]) ? val : -val;
                }
            }
        }
        return total;
    }

    private int getPieceValue(String p) {
        switch (p.toLowerCase()) {
            case "p": return 100; case "n": return 300; case "b": return 300;
            case "r": return 500; case "q": return 900; case "k": return 10000;
            default: return 0;
        }
    }

    // --- Piece Movement and Rules ---
    private boolean isValidMove(int r1, int c1, int r2, int c2, boolean checkKing) {
        if (r1 == r2 && c1 == c2) return false;
        String p = board[r1][c1];
        if (p == null || (board[r2][c2] != null && isWhite(p) == isWhite(board[r2][c2]))) return false;

        boolean v = false;
        int dr = Math.abs(r2-r1), dc = Math.abs(c2-c1);
        String type = p.toLowerCase();

        if (type.equals("p")) v = validatePawn(r1, c1, r2, c2, p);
        else if (type.equals("r")) v = (r1 == r2 || c1 == c2) && isPathClear(r1, c1, r2, c2);
        else if (type.equals("n")) v = dr * dc == 2;
        else if (type.equals("b")) v = dr == dc && isPathClear(r1, c1, r2, c2);
        else if (type.equals("q")) v = (dr == dc || r1 == r2 || c1 == c2) && isPathClear(r1, c1, r2, c2);
        else if (type.equals("k")) v = (dr <= 1 && dc <= 1) || (dr == 0 && dc == 2 && canCastle(r1, c1, r2, c2));

        return v && (!checkKing || !leavesKingInCheck(r1, c1, r2, c2));
    }

    private void executeMove(int r1, int c1, int r2, int c2) {
        if (board[r1][c1].equalsIgnoreCase("k") && Math.abs(c2-c1) == 2) {
            int rkC = (c2 > c1) ? 7 : 0;
            int nRkC = (c2 > c1) ? 5 : 3;
            board[r1][nRkC] = board[r1][rkC]; board[r1][rkC] = null;
        }
        board[r2][c2] = board[r1][c1]; board[r1][c1] = null;
        if(r1==7 && c1==4) wKMoved=true; if(r1==0 && c1==4) bKMoved=true;
    }

    private boolean isPathClear(int r1, int c1, int r2, int c2) {
        int dr = Integer.compare(r2, r1), dc = Integer.compare(c2, c1);
        int cr = r1+dr, cc = c1+dc;
        while (cr != r2 || cc != c2) {
            if (board[cr][cc] != null) return false;
            cr += dr; cc += dc;
        }
        return true;
    }

    private boolean validatePawn(int r1, int c1, int r2, int c2, String p) {
        int d = isWhite(p) ? -1 : 1;
        if (c1 == c2 && board[r2][c2] == null) {
            if (r2 == r1 + d) return true;
            if ((r1 == 1 || r1 == 6) && r2 == r1 + 2*d) return board[r1+d][c1] == null;
        }
        return Math.abs(c2-c1) == 1 && r2 == r1 + d && board[r2][c2] != null;
    }

    private boolean canCastle(int r, int c, int tr, int tc) {
        if (isInCheck(whiteTurn) || (whiteTurn ? wKMoved : bKMoved)) return false;
        return isPathClear(r, c, r, (tc > c ? 7 : 0));
    }

    private boolean leavesKingInCheck(int r1, int c1, int r2, int c2) {
        String s = board[r1][c1], t = board[r2][c2];
        board[r2][c2] = s; board[r1][c1] = null;
        boolean check = isInCheck(isWhite(s));
        board[r1][c1] = s; board[r2][c2] = t;
        return check;
    }

    private boolean isInCheck(boolean white) {
        int kr = -1, kc = -1;
        for(int r=0; r<8; r++) for(int c=0; c<8; c++) 
            if(board[r][c]!=null && board[r][c].equalsIgnoreCase("k") && isWhite(board[r][c])==white) { kr=r; kc=c; }
        if (kr == -1) return false;
        for(int r=0; r<8; r++) for(int c=0; c<8; c++) 
            if(board[r][c]!=null && isWhite(board[r][c])!=white && isValidMove(r, c, kr, kc, false)) return true;
        return false;
    }

    private boolean checkGameState() {
        if (getAllLegalMoves(whiteTurn).isEmpty()) {
            JOptionPane.showMessageDialog(this, isInCheck(whiteTurn) ? "Checkmate!" : "Stalemate!");
            return true;
        }
        return false;
    }

    private List<Move> getAllLegalMoves(boolean white) {
        List<Move> m = new ArrayList<Move>();
        for(int r=0; r<8; r++) for(int c=0; c<8; c++)
            if(board[r][c]!=null && isWhite(board[r][c])==white)
                for(int tr=0; tr<8; tr++) for(int tc=0; tc<8; tc++)
                    if(isValidMove(r, c, tr, tc, true)) m.add(new Move(r, c, tr, tc));
        return m;
    }

    private boolean isWhite(String p) { return Character.isLowerCase(p.charAt(0)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font f = new Font("SansSerif", Font.PLAIN, 60); g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                g2.setColor((r + c) % 2 == 0 ? new Color(240, 217, 181) : new Color(181, 136, 99));
                if (r == selR && c == selC) g2.setColor(new Color(130, 151, 105));
                g2.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (board[r][c] != null) {
                    g2.setColor(isWhite(board[r][c]) ? Color.WHITE : Color.BLACK);
                    String s = getUni(board[r][c]);
                    g2.drawString(s, c*TILE_SIZE+(TILE_SIZE-fm.stringWidth(s))/2, r*TILE_SIZE+((TILE_SIZE-fm.getHeight())/2)+fm.getAscent());
                }
            }
        }
    }

    private String getUni(String p) {
        switch (p) {
            case "R": return "\u265C"; case "N": return "\u265E"; case "B": return "\u265D";
            case "Q": return "\u265B"; case "K": return "\u265A"; case "P": return "\u265F";
            case "r": return "\u265C"; case "n": return "\u265E"; case "b": return "\u265D";
            case "q": return "\u265B"; case "k": return "\u265A"; case "p": return "\u265F";
            default: return "";
        }
    }

    static class Move { int r1, c1, r2, c2; Move(int r1, int c1, int r2, int c2) { this.r1=r1; this.c1=c1; this.r2=r2; this.c2=c2; } }
}
