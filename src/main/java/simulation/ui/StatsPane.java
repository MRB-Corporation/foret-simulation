package simulation.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import simulation.entities.GridStats;

/**
 * JavaFX panel showing live simulation statistics.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Text labels: step, burning/burned/intact counts and percentages</li>
 *   <li>Trend indicator (↑ spreading / ↓ receding / = stable)</li>
 *   <li>{@link LineChart} — burning cell count over time</li>
 *   <li>{@link BarChart}  — terrain type distribution</li>
 * </ul>
 */
public class StatsPane extends VBox {

    private static final int MAX_HISTORY = 60;

    // ── Text labels ───────────────────────────────────────────────────────────

    private final Label lblStep    = label("Step: 0");
    private final Label lblBurn    = label("Burning:  0  (0.0%)");
    private final Label lblBurned  = label("Burned:   0  (0.0%)");
    private final Label lblIntact  = label("Intact:   0  (0.0%)");
    private final Label lblTrend   = label("Trend: —");
    private final Label lblHum     = label("Avg humidity:     0.00");
    private final Label lblTemp    = label("Avg temperature:  0.0 °C");
    private final Label lblInflam  = label("Avg inflamm.:     0.00");

    // ── Line chart: burning over time ─────────────────────────────────────────

    private final XYChart.Series<Number, Number> burnSeries  = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> burnedSeries = new XYChart.Series<>();
    private int chartStep = 0;

    // ── Bar chart: terrain distribution ──────────────────────────────────────

    private final XYChart.Series<String, Number> terrainSeries = new XYChart.Series<>();

    // ── Trend helper ──────────────────────────────────────────────────────────

    private final int[] lastBurning = new int[4];
    private int trendIdx = 0;

    /**
     * Builds the statistics pane with all charts initialised.
     */
    public StatsPane() {
        setSpacing(6);
        setPadding(new Insets(8));
        setPrefWidth(220);
        setStyle("-fx-background-color: #f8f8f8;");

        // ── Section: simulation ───────────────────────────────────────────────
        getChildren().add(sectionLabel("── Simulation ──"));
        getChildren().addAll(lblStep, lblBurn, lblBurned, lblIntact, lblTrend);

        // ── Section: averages ─────────────────────────────────────────────────
        getChildren().add(sectionLabel("── Averages ──"));
        getChildren().addAll(lblHum, lblTemp, lblInflam);

        // ── Line chart ────────────────────────────────────────────────────────
        getChildren().add(sectionLabel("── Fire progress ──"));
        burnSeries.setName("Burning");
        burnedSeries.setName("Burned");

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Step");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Cells");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.getData().addAll(burnSeries, burnedSeries);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(true);
        lineChart.setPrefHeight(160);
        lineChart.setAnimated(false);
        getChildren().add(lineChart);

        // ── Bar chart ─────────────────────────────────────────────────────────
        getChildren().add(sectionLabel("── Terrain types ──"));
        terrainSeries.setName("Cells");

        CategoryAxis catAxis = new CategoryAxis(FXCollections.observableArrayList(
                "Forest", "Wet", "Firebreak", "Water", "Urban"));
        NumberAxis numAxis = new NumberAxis();

        BarChart<String, Number> barChart = new BarChart<>(catAxis, numAxis);
        barChart.getData().add(terrainSeries);
        barChart.setPrefHeight(160);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        getChildren().add(barChart);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Refreshes all labels and charts with the latest statistics.
     * Safe to call from any thread.
     *
     * @param stats newly computed stats
     */
    public void update(GridStats stats) {
        Platform.runLater(() -> {
            lblStep.setText("Step: " + stats.getStep());
            lblBurn.setText(String.format("Burning:  %d  (%.1f%%)",
                    stats.getBurningCells(), stats.getBurningPercent()));
            lblBurned.setText(String.format("Burned:   %d  (%.1f%%)",
                    stats.getBurnedCells(), stats.getBurnedPercent()));
            lblIntact.setText(String.format("Intact:   %d  (%.1f%%)",
                    stats.getIntactCells(), stats.getIntactPercent()));
            lblTrend.setText("Trend: " + computeTrend(stats.getBurningCells()));
            lblHum.setText(String.format("Avg humidity:     %.2f", stats.getAvgHumidity()));
            lblTemp.setText(String.format("Avg temperature:  %.1f °C", stats.getAvgTemperature()));
            lblInflam.setText(String.format("Avg inflamm.:     %.2f", stats.getAvgInflammability()));

            // Line chart
            chartStep = stats.getStep();
            burnSeries.getData().add(new XYChart.Data<>(chartStep, stats.getBurningCells()));
            burnedSeries.getData().add(new XYChart.Data<>(chartStep, stats.getBurnedCells()));
            if (burnSeries.getData().size() > MAX_HISTORY) {
                burnSeries.getData().remove(0);
                burnedSeries.getData().remove(0);
            }

            // Bar chart
            @SuppressWarnings("unchecked")
            ObservableList<XYChart.Data<String, Number>> bars =
                    FXCollections.observableArrayList(
                        new XYChart.Data<>("Forest",   stats.getForestCells()),
                        new XYChart.Data<>("Wet",      stats.getWetZoneCells()),
                        new XYChart.Data<>("Firebreak",stats.getFirebreakCells()),
                        new XYChart.Data<>("Water",    stats.getWaterCells()),
                        new XYChart.Data<>("Urban",    stats.getUrbanCells())
                    );
            terrainSeries.setData(bars);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String computeTrend(int currentBurning) {
        lastBurning[trendIdx % lastBurning.length] = currentBurning;
        trendIdx++;
        if (trendIdx < lastBurning.length) return "—";
        int oldest = lastBurning[trendIdx % lastBurning.length];
        if (currentBurning > oldest + 2) return "↑ spreading";
        if (currentBurning < oldest - 2) return "↓ receding";
        return "= stable";
    }

    private static Label label(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        return lbl;
    }

    private static Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #555;");
        return lbl;
    }
}
