package view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.Cell;
import model.CellState;
import model.Grid;
import simulation.Simulator;

/**
 * Panneau qui affiche les statistiques de la grille en direct :
 * nombre et pourcentage de cellules par état,
 * moyennes d'humidité et de température.
 */
public class StatsPanel extends VBox {

    /** Le simulateur. */
    private final Simulator simulator;

    private final Label lblIntact   = new Label();
    private final Label lblBurning  = new Label();
    private final Label lblBurned   = new Label();
    private final Label lblHumidity = new Label();
    private final Label lblTemp     = new Label();

    /**
     * Crée le panneau de statistiques.
     *
     * @param simulator le simulateur
     */
    public StatsPanel(Simulator simulator) {
        this.simulator = simulator;

        setSpacing(8);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("Statistiques");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        getChildren().addAll(
            title, lblIntact, lblBurning, lblBurned, lblHumidity, lblTemp
        );

        refresh();
    }

    /**
     * Recalcule et affiche toutes les statistiques.
     * Appelée à chaque pas de simulation.
     */
    public void refresh() {
        Grid grid = simulator.getGrid();
        int total = grid.getWidth() * grid.getHeight();

        int intact = 0, burning = 0, burned = 0;
        double sumHumidity = 0.0, sumTemp = 0.0;

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell c = grid.getCell(x, y);

                if (c.getState() == CellState.INTACT)       intact++;
                else if (c.getState() == CellState.BURNING) burning++;
                else                                        burned++;

                sumHumidity += c.getHumidity();
                sumTemp     += c.getTemperature();
            }
        }

        lblIntact.setText(String.format("Intactes : %d (%.1f%%)", intact,  100.0 * intact  / total));
        lblBurning.setText(String.format("En feu : %d (%.1f%%)",  burning, 100.0 * burning / total));
        lblBurned.setText(String.format("Brûlées : %d (%.1f%%)",  burned,  100.0 * burned  / total));
        lblHumidity.setText(String.format("Humidité moyenne : %.2f", sumHumidity / total));
        lblTemp.setText(String.format("Température moyenne : %.1f °C", sumTemp / total));
    }
}