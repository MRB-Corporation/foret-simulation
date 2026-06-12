package simulation.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import simulation.services.Simulator;

/**
 * JavaFX application entry point.
 *
 * <p>Assembles the main layout:</p>
 * <ul>
 *   <li>Centre — {@link GridCanvas}: simulation grid</li>
 *   <li>Right  — {@link StatsPane}: live statistics + charts</li>
 *   <li>Bottom — {@link ControlPane}: all controls</li>
 *   <li>Left   — {@link LegendPane}: colour legend</li>
 * </ul>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Simulator   simulator   = new Simulator();
        GridCanvas  gridCanvas  = new GridCanvas(simulator);
        StatsPane   statsPane   = new StatsPane();
        ControlPane controlPane = new ControlPane(simulator, gridCanvas, statsPane);
        LegendPane  legendPane  = new LegendPane();

        // Wire simulator refresh callback
        simulator.setOnRefresh(grid -> {
            gridCanvas.drawGrid(grid);
            statsPane.update(simulator.computeStats());
        });

        // Wrap canvas in a Pane so it stretches with the window
        Pane canvasWrapper = new Pane(gridCanvas);
        gridCanvas.widthProperty().bind(canvasWrapper.widthProperty());
        gridCanvas.heightProperty().bind(canvasWrapper.heightProperty());

        // Redraw when canvas is resized
        gridCanvas.widthProperty().addListener((o, old, val) -> {
            if (val.doubleValue() > 0) gridCanvas.drawGrid(simulator.getGrid());
        });
        gridCanvas.heightProperty().addListener((o, old, val) -> {
            if (val.doubleValue() > 0) gridCanvas.drawGrid(simulator.getGrid());
        });

        BorderPane root = new BorderPane();
        root.setCenter(canvasWrapper);
        root.setRight(statsPane);
        root.setBottom(controlPane);
        root.setLeft(legendPane);

        Scene scene = new Scene(root, 1280, 800);

        primaryStage.setTitle("Forest Fire Spread Simulation");
        primaryStage.setScene(scene);

        // Reset zoom AFTER the stage is fully shown so canvas has real dimensions
        primaryStage.setOnShown(e -> {
            gridCanvas.resetZoom();
            statsPane.update(simulator.computeStats());
        });

        primaryStage.show();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
