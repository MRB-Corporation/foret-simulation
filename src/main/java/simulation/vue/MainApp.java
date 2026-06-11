package simulation.vue;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simulation.Simulator;

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
 *
 * <p>Launch command:</p>
 * <pre>
 * java --module-path "PATH_TO_JAVAFX\lib"
 *      --add-modules javafx.controls,javafx.fxml
 *      -cp bin simulation.vue.MainApp
 * </pre>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Simulator simulator = new Simulator();

        GridCanvas   gridCanvas   = new GridCanvas(simulator);
        StatsPane    statsPane    = new StatsPane();
        ControlPane  controlPane  = new ControlPane(simulator, gridCanvas, statsPane);
        LegendPane   legendPane   = new LegendPane();

        // Wire simulator → canvas + stats refresh
        simulator.setOnRefresh(grid -> {
            gridCanvas.drawGrid(grid);
            statsPane.update(simulator.computeStats());
        });

        BorderPane root = new BorderPane();
        root.setCenter(gridCanvas);
        root.setRight(statsPane);
        root.setBottom(controlPane);
        root.setLeft(legendPane);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
                getClass().getResource("/simulation/vue/style.css") != null
                ? getClass().getResource("/simulation/vue/style.css").toExternalForm()
                : "");

        primaryStage.setTitle("Forest Fire Spread Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial draw
        gridCanvas.drawGrid(simulator.getGrid());
        statsPane.update(simulator.computeStats());
    }

    /**
     * Application entry point — delegates to JavaFX launch mechanism.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
