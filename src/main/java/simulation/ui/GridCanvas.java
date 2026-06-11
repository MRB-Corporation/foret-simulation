package simulation.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import simulation.services.Simulator;
import simulation.entities.Cell;
import simulation.entities.CellState;
import simulation.entities.Grid;
import simulation.entities.TerrainType;
import simulation.config.Constants;

/**
 * JavaFX {@link Canvas} that renders the simulation grid.
 *
 * <p>Interaction:</p>
 * <ul>
 *   <li><b>Left click</b> — ignite a cell</li>
 *   <li><b>Right drag</b> — brush: paint terrain</li>
 *   <li><b>Shift + drag</b> — zone selection, applied on release</li>
 *   <li><b>Scroll wheel</b> — zoom in / out</li>
 * </ul>
 */
public class GridCanvas extends Canvas {

    private static final int MIN_CELL = 2;
    private static final int MAX_CELL = 60;

    private final Simulator simulator;
    private int cellSize = Constants.CELL_SIZE_PX;

    private int offsetX = 0;
    private int offsetY = 0;

    // Zone selection state
    private boolean zoneActive = false;
    private int zoneX1, zoneY1, zoneX2, zoneY2;

    private TerrainType paintTerrain = TerrainType.FOREST;

    /**
     * Creates the grid canvas bound to the given simulator.
     *
     * @param simulator the running simulator
     */
    public GridCanvas(Simulator simulator) {
        super(800, 600);
        this.simulator = simulator;
        registerHandlers();
    }

    // ── Mouse handlers ────────────────────────────────────────────────────────

    private void registerHandlers() {

        setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.PRIMARY && !e.isShiftDown()) {
                int[] cell = toCell(e.getX(), e.getY());
                simulator.igniteCell(cell[0], cell[1]);
            }
        });

        setOnMousePressed((MouseEvent e) -> {
            if (e.isShiftDown()) {
                int[] cell = toCell(e.getX(), e.getY());
                zoneX1 = zoneX2 = cell[0];
                zoneY1 = zoneY2 = cell[1];
                zoneActive = true;
            }
        });

        setOnMouseDragged((MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Right drag = brush paint
                int[] cell = toCell(e.getX(), e.getY());
                simulator.paintZone(cell[0], cell[1], cell[0], cell[1], paintTerrain);
            }
            if (zoneActive && e.isShiftDown()) {
                int[] cell = toCell(e.getX(), e.getY());
                zoneX2 = cell[0];
                zoneY2 = cell[1];
                drawGrid(simulator.getGrid()); // refresh selection overlay
            }
        });

        setOnMouseReleased((MouseEvent e) -> {
            if (zoneActive) {
                int[] cell = toCell(e.getX(), e.getY());
                zoneX2 = cell[0];
                zoneY2 = cell[1];
                simulator.paintZone(zoneX1, zoneY1, zoneX2, zoneY2, paintTerrain);
                zoneActive = false;
            }
        });

        setOnScroll((ScrollEvent e) -> {
            if (e.getDeltaY() > 0) zoomIn(); else zoomOut();
        });
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /**
     * Redraws the entire grid on the canvas.
     *
     * @param grid current grid state
     */
    public void drawGrid(Grid grid) {
        double w = getWidth();
        double h = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(35, 35, 35));
        gc.fillRect(0, 0, w, h);

        if (grid == null) return;

        int t = cellSize;
        int cols = (int) Math.ceil(w / t) + 1;
        int rows = (int) Math.ceil(h / t) + 1;

        int endX = Math.min(grid.getWidth(),  offsetX + cols);
        int endY = Math.min(grid.getHeight(), offsetY + rows);

        for (int y = offsetY; y < endY; y++) {
            for (int x = offsetX; x < endX; x++) {
                Cell c = grid.getCell(x, y);
                gc.setFill(colorFor(c));
                double px = (x - offsetX) * t;
                double py = (y - offsetY) * t;
                gc.fillRect(px, py, t, t);
                
                // Dessiner un arbre si c'est une forêt intacte et qu'on a assez de place
                if (t >= 8 && c.getState() == CellState.INTACT && c.getTerrain() == TerrainType.FOREST) {
                    drawTree(gc, px, py, t);
                }
            }
        }

        // Grid lines (only if cells are large enough to avoid graying out the screen)
        if (t >= 4) {
            gc.setStroke(Color.rgb(0, 0, 0, 0.15));
            gc.setLineWidth(0.5);
            int drawnCols = Math.max(0, endX - offsetX);
            int drawnRows = Math.max(0, endY - offsetY);
            for (int x = 0; x <= drawnCols; x++) gc.strokeLine(x * t, 0, x * t, drawnRows * t);
            for (int y = 0; y <= drawnRows; y++) gc.strokeLine(0, y * t, drawnCols * t, y * t);
        }

        // Zone selection overlay
        if (zoneActive) {
            int sx = (Math.min(zoneX1, zoneX2) - offsetX) * t;
            int sy = (Math.min(zoneY1, zoneY2) - offsetY) * t;
            int sw = (Math.abs(zoneX2 - zoneX1) + 1) * t;
            int sh = (Math.abs(zoneY2 - zoneY1) + 1) * t;
            gc.setFill(Color.rgb(255, 255, 0, 0.25));
            gc.fillRect(sx, sy, sw, sh);
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(1.5);
            gc.strokeRect(sx, sy, sw, sh);
        }
    }

    private void drawTree(GraphicsContext gc, double x, double y, double size) {
        // Tronc
        gc.setFill(Color.rgb(139, 69, 19)); // Marron
        double trunkW = size * 0.2;
        double trunkH = size * 0.3;
        double trunkX = x + (size - trunkW) / 2;
        double trunkY = y + size - trunkH;
        gc.fillRect(trunkX, trunkY, trunkW, trunkH);

        // Feuilles (3 petits triangles superposés)
        gc.setFill(Color.LIMEGREEN);
        gc.fillPolygon(new double[]{x + size*0.1, x + size*0.5, x + size*0.9},
                       new double[]{y + size*0.7, y + size*0.3, y + size*0.7}, 3);
        gc.fillPolygon(new double[]{x + size*0.15, x + size*0.5, x + size*0.85},
                       new double[]{y + size*0.5,  y + size*0.15, y + size*0.5}, 3);
        gc.fillPolygon(new double[]{x + size*0.2, x + size*0.5, x + size*0.8},
                       new double[]{y + size*0.3, y + size*0.0,  y + size*0.3}, 3);
    }

    private Color colorFor(Cell c) {
        if (c.getState() == CellState.ON_FIRE) {
            // Gradation : du jaune brillant (nouveau feu) au rouge foncé (fin de feu)
            double ratio = Math.max(0.0, Math.min(1.0, c.getBurnCounter() / 15.0));
            int r = (int)(139 + (255 - 139) * ratio); // 139 à 255
            int g = (int)(255 * ratio);               // 0 à 255
            return Color.rgb(r, g, 0);
        }
        if (c.getState() == CellState.BURNED)  return Color.rgb( 50,  30,  10);
        return switch (c.getTerrain()) {
            case FOREST     -> Color.rgb( 34, 139,  34);
            case WET_ZONE   -> Color.rgb( 32, 178, 170);
            case FIREBREAK  -> Color.rgb(160, 140, 100);
            case WATER      -> Color.rgb( 30, 100, 200);
            case URBAN_ZONE -> Color.rgb(180, 180, 180);
        };
    }

    // ── Zoom ──────────────────────────────────────────────────────────────────

    /** Increases cell size. */
    public void zoomIn()    { setCellSize(cellSize + 4); }

    /** Decreases cell size. */
    public void zoomOut()   { setCellSize(cellSize - 4); }

    /** Resets cell size to fit the screen. */
    public void resetZoom() {
        if (simulator.getGrid() == null) return;
        double w = getWidth();
        double h = getHeight();
        int cellW = (int) (w / simulator.getGrid().getWidth());
        int cellH = (int) (h / simulator.getGrid().getHeight());
        cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.min(cellW, cellH)));
        offsetX = 0;
        offsetY = 0;
        drawGrid(simulator.getGrid());
    }

    private void setCellSize(int size) {
        cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, size));
        drawGrid(simulator.getGrid());
    }

    // ── Paint terrain ─────────────────────────────────────────────────────────

    /**
     * Sets the terrain type used for brush/zone painting.
     *
     * @param terrain terrain to paint
     */
    public void setPaintTerrain(TerrainType terrain) {
        this.paintTerrain = terrain;
    }

    // ── Camera ────────────────────────────────────────────────────────────────

    /** Moves the camera offset. */
    public void moveCamera(int dx, int dy) {
        offsetX = Math.max(0, offsetX + dx);
        offsetY = Math.max(0, offsetY + dy);
        drawGrid(simulator.getGrid());
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private int[] toCell(double px, double py) {
        return new int[]{ offsetX + (int)(px / cellSize), offsetY + (int)(py / cellSize) };
    }
}
