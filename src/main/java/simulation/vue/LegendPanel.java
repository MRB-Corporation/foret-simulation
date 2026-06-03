package simulation.vue;

import javax.swing.*;
import java.awt.*;

public class LegendPanel extends JPanel {

    public LegendPanel() {
        setPreferredSize(new Dimension(160, 300));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 8;
        int y = 12;
        int box = 18;
        int gap = 8;

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString("Légende", x, y);
        y += 18;
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));

        drawItem(g2, x, y, box, new Color(255, 80, 0), "En feu"); y += box + gap;
        drawItem(g2, x, y, box, new Color(50, 30, 10), "Brûlé"); y += box + gap;
        drawItem(g2, x, y, box, new Color(34, 139, 34), "Forêt"); y += box + gap;
        drawItem(g2, x, y, box, new Color(32, 178, 170), "Zone humide"); y += box + gap;
        drawItem(g2, x, y, box, new Color(160, 140, 100), "Coupe-feu"); y += box + gap;
        drawItem(g2, x, y, box, new Color(30, 100, 200), "Eau"); y += box + gap;
        drawItem(g2, x, y, box, new Color(180, 180, 180), "Zone urbanisée");
    }

    private void drawItem(Graphics2D g2, int x, int y, int box, Color color, String label) {
        g2.setColor(color);
        g2.fillRect(x, y - box + 4, box, box);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y - box + 4, box, box);
        g2.drawString(label, x + box + 8, y - box + 4 + (box - 4));
    }
}
