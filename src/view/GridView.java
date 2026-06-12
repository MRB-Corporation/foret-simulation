package view;

import config.SimulationConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.Cell;
import model.CellState;
import model.Grid;
import simulation.Simulator;

/**
 * Composant JavaFX qui dessine la grille.
 * Chaque cellule est un carré coloré selon son état et son terrain.
 * Un clic sur une cellule allume le feu.
 */
public class GridView extends Pane {

    /** Taille d'une cellule en pixels. */
    private static final int CELL_SIZE = 12;

    /** Le simulateur. */
    private final Simulator simulator;

    /** Le canvas de dessin. */
    private final Canvas canvas;

    /**
     * Crée la vue de la grille.
     *
     * @param simulator le simulateur
     */
    public GridView(Simulator simulator) {
        this.simulator = simulator;

        SimulationConfig config = simulator.getConfig();
        canvas = new Canvas(
            config.gridWidth  * CELL_SIZE,
            config.gridHeight * CELL_SIZE
        );
        getChildren().add(canvas);

        // Clic souris → allumer le feu
        canvas.setOnMouseClicked(this::handleClick);

        refresh();
    }

    /**
     * Redessine toute la grille.
     * Appelée à chaque pas de simulation.
     */
    public void refresh() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Grid grid = simulator.getGrid();

        // Adapter la taille du canvas si la grille a changé (chargement fichier)
        canvas.setWidth(grid.getWidth() * CELL_SIZE);
        canvas.setHeight(grid.getHeight() * CELL_SIZE);

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                gc.setFill(getColor(grid.getCell(x, y)));
                gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Retourne la couleur d'une cellule selon son état puis son terrain.
     *
     * @param cell la cellule
     * @return la couleur à dessiner
     */
    private Color getColor(Cell cell) {
        // L'état est prioritaire sur le terrain
        if (cell.getState() == CellState.BURNING) return Color.rgb(255, 80, 0);
        if (cell.getState() == CellState.BURNED)  return Color.rgb(50, 30, 10);

        switch (cell.getTerrain()) {
            case FOREST:    return Color.rgb(34, 139, 34);
            case WET_ZONE:  return Color.rgb(32, 178, 170);
            case FIREBREAK: return Color.rgb(160, 140, 100);
            case WATER:     return Color.rgb(30, 100, 200);
            case URBAN:     return Color.rgb(180, 180, 180);
            default:        return Color.BLACK;
        }
    }

    /**
     * Convertit un clic souris en coordonnées grille
     * et allume le feu sur la cellule cliquée.
     *
     * @param event l'événement souris
     */
    private void handleClick(MouseEvent event) {
        int x = (int) (event.getX() / CELL_SIZE);
        int y = (int) (event.getY() / CELL_SIZE);
        simulator.igniteCell(x, y);
        refresh();
    }
}