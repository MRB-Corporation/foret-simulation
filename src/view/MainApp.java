package view;

import config.SimulationConfig;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simulation.Simulator;

/**
 * Point d'entrée de l'application JavaFX.
 * Assemble la grille, le panneau de contrôle et les statistiques,
 * puis lance le timer qui fait avancer la simulation.
 */
public class MainApp extends Application {

    /** Délai entre deux pas de simulation, en nanosecondes (modifiable). */
    private long stepDelayNs = 500_000_000L; // 500 ms par défaut

    /** Le simulateur principal. */
    private Simulator simulator;

    /** Temps du dernier pas (pour le timer). */
    private long lastUpdate = 0;

    /**
     * Méthode principale JavaFX : crée la fenêtre et les composants.
     *
     * @param stage la fenêtre principale
     */
    @Override
    public void start(Stage stage) {

        SimulationConfig config = new SimulationConfig();
        simulator = new Simulator(config);

        // Composants de l'interface
        GridView gridView       = new GridView(simulator);
        StatsPanel statsPanel   = new StatsPanel(simulator);
        ControlPanel controlPanel = new ControlPanel(simulator, gridView, statsPanel, this);

        // Disposition : grille au centre, stats à droite, contrôles en bas
        BorderPane root = new BorderPane();
        root.setCenter(gridView);
        root.setRight(statsPanel);
        root.setBottom(controlPanel);

        // Timer : appelle nextStep() à intervalle régulier
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= stepDelayNs) {
                    if (simulator.isRunning()) {
                        simulator.nextStep();
                        gridView.refresh();
                        statsPanel.refresh();
                        controlPanel.refreshStep();
                    }
                    lastUpdate = now;
                }
            }
        };
        timer.start();

        Scene scene = new Scene(root, 1100, 750);
        stage.setTitle("Simulation de feu de forêt");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Modifie la vitesse de la simulation.
     *
     * @param ms délai entre deux pas, en millisecondes
     */
    public void setStepDelayMs(long ms) {
        this.stepDelayNs = ms * 1_000_000L;
    }

    /**
     * Point d'entrée Java standard.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}