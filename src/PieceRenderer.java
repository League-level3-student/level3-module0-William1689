import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PieceRenderer {
    private static Map<String, Image> pieceImages = new HashMap<>();

    public static void loadImages(int tileSize) {
        String[] colors = {"w", "b"};
        String[] types = {"p", "r", "n", "b", "q", "k"}; // n = Knight
        try {
            for (String c : colors) {
                for (String t : types) {
                    String key = c + t;
                    // Loads from a folder named 'images' in your project root
                    File imgFile = new File("images/" + key + ".png");
                    if (imgFile.exists()) {
                        Image img = ImageIO.read(imgFile);
                        pieceImages.put(key, img.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    public static void drawPiece(Graphics g, ChessGame.Piece p, int x, int y) {
        String key = (p.isWhite ? "w" : "b") + getTypeCode(p.type);
        Image img = pieceImages.get(key);
        
        if (img != null) {
            g.drawImage(img, x, y, null);
        } else {
            // Fallback to text if image is missing
            g.setColor(p.isWhite ? Color.WHITE : Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString(p.getSymbol(), x + 25, y + 55);
        }
    }

    private static String getTypeCode(ChessGame.PieceType type) {
        switch(type) {
            case PAWN: return "p"; case ROOK: return "r";
            case KNIGHT: return "n"; case BISHOP: return "b";
            case QUEEN: return "q"; case KING: return "k";
            default: return "";
        }
    }
}
