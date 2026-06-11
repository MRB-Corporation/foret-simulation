package simulation.vue;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX panel displaying the colour legend for terrain types and fire states.
 */
public class LegendPane extends VBox {

    private static final int BOX = 16;
    private static final int W   = 150;
    private static final int H   = 220;

    /** Builds the legend pane. */
    public LegendPane() {
        setPadding(new Insets(8));
        setStyle("-fx-background-color: white;");
        Canvas canvas = new Canvas(W, H);
        drawLegend(canvas.getGraphicsContext2D());
        getChildren().add(canvas);
    }

    private void drawLegend(GraphicsContext gc) {
        int x = 6, y = 16, gap = 26;

        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Legend", x, y);
        y += 10;

        gc.setFont(Font.font("System", 11));
        drawEntry(gc, x, y, Color.rgb(255, 80,   0), "On fire");   y += gap;
        drawEntry(gc, x, y, Color.rgb( 50, 30,  10), "Burned");    y += gap;
        drawEntry(gc, x, y, Color.rgb( 34,139,  34), "Forest");    y += gap;
        drawEntry(gc, x, y, Color.rgb( 32,178, 170), "Wet zone");  y += gap;
        drawEntry(gc, x, y, Color.rgb(160,140, 100), "Firebreak"); y += gap;
        drawEntry(gc, x, y, Color.rgb( 30,100, 200), "Water");     y += gap;
        drawEntry(gc, x, y, Color.rgb(180,180, 180), "Urban zone");
    }

    private void drawEntry(GraphicsContext gc, int x, int y, Color color, String label) {
        gc.setFill(color);
        gc.fillRect(x, y, BOX, BOX);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, BOX, BOX);
        gc.setFill(Color.BLACK);
        gc.fillText(label, x + BOX + 6, y + BOX - 3);
    }
}
