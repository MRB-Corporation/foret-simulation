package view;

import config.SimulationConfig;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Grid;
import simulation.GridFileManager;
import simulation.Simulator;

import java.io.File;
import java.io.IOException;

/**
 * Panneau de contrôle de la simulation.
 * Contient les boutons (démarrer, pause, reset, pas-à-pas,
 * sauvegarder, charger) et les sliders de paramètres.
 */
public class ControlPanel extends VBox {

    /** Le simulateur. */
    private final Simulator simulator;

    /** La vue de la grille. */
    private final GridView gridView;

    /** Le panneau de statistiques. */
    private final StatsPanel statsPanel;

    /** L'application principale (pour régler la vitesse). */
    private final MainApp mainApp;

    /** Label du pas actuel. */
    private final Label stepLabel = new Label("Pas : 0");

    /**
     * Crée le panneau de contrôle.
     *
     * @param simulator  le simulateur
     * @param gridView   la vue de la grille
     * @param statsPanel le panneau de statistiques
     * @param mainApp    l'application principale
     */
    public ControlPanel(Simulator simulator, GridView gridView,
                        StatsPanel statsPanel, MainApp mainApp) {
        this.simulator  = simulator;
        this.gridView   = gridView;
        this.statsPanel = statsPanel;
        this.mainApp    = mainApp;

        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f0f0f0;");

        build();
    }

    /**
     * Construit tous les composants du panneau.
     */
    private void build() {
        SimulationConfig config = simulator.getConfig();

        // ── Ligne 1 : boutons principaux ──────────────────────────────────
        Button btnStart = new Button("Démarrer");
        Button btnPause = new Button("Pause");
        Button btnStep  = new Button("Pas à pas");
        Button btnReset = new Button("Réinitialiser");
        Button btnSave  = new Button("Sauvegarder");
        Button btnLoad  = new Button("Charger");

        btnStart.setOnAction(e -> simulator.start());
        btnPause.setOnAction(e -> simulator.pause());

        btnStep.setOnAction(e -> {
            simulator.singleStep();
            refreshAll();
        });

        btnReset.setOnAction(e -> {
            simulator.reset();
            refreshAll();
        });

        btnSave.setOnAction(e -> saveGrid());
        btnLoad.setOnAction(e -> loadGrid());

        CheckBox chkToric = new CheckBox("Topologie torique");
        chkToric.setSelected(config.toricTopology);
        chkToric.setOnAction(e -> config.toricTopology = chkToric.isSelected());

        HBox row1 = new HBox(10, btnStart, btnPause, btnStep, btnReset,
                             btnSave, btnLoad, chkToric, stepLabel);

        // ── Ligne 2 : vitesse de simulation ────────────────────────────────
        Label lblSpeed = new Label("Vitesse : 500 ms");
        Slider sliderSpeed = new Slider(50, 2000, 500);
        sliderSpeed.setShowTickMarks(true);
        sliderSpeed.setMajorTickUnit(500);
        sliderSpeed.valueProperty().addListener((obs, oldV, newV) -> {
            mainApp.setStepDelayMs(newV.longValue());
            lblSpeed.setText("Vitesse : " + newV.intValue() + " ms");
        });

        HBox row2 = new HBox(10, lblSpeed, sliderSpeed);

        // ── Ligne 3 : paramètres de propagation ────────────────────────────
        Label lblIgnition = new Label(
            String.format("Température ignition : %.0f °C", config.ignitionTemperature));
        Slider sliderIgnition = new Slider(50, 300, config.ignitionTemperature);
        sliderIgnition.valueProperty().addListener((obs, oldV, newV) -> {
            config.ignitionTemperature = newV.doubleValue();
            lblIgnition.setText(
                String.format("Température ignition : %.0f °C", config.ignitionTemperature));
        });

        Label lblRadius = new Label("Rayon : " + config.neighborRadius);
        Slider sliderRadius = new Slider(1, 4, config.neighborRadius);
        sliderRadius.setSnapToTicks(true);
        sliderRadius.setMajorTickUnit(1);
        sliderRadius.setMinorTickCount(0);
        sliderRadius.valueProperty().addListener((obs, oldV, newV) -> {
            config.neighborRadius = newV.intValue();
            lblRadius.setText("Rayon : " + config.neighborRadius);
        });

        CheckBox chkDiagonals = new CheckBox("Diagonales (8 voisins)");
        chkDiagonals.setSelected(config.useDiagonals);
        chkDiagonals.setOnAction(e -> config.useDiagonals = chkDiagonals.isSelected());

        HBox row3 = new HBox(10, lblIgnition, sliderIgnition,
                             lblRadius, sliderRadius, chkDiagonals);

        getChildren().addAll(row1, row2, row3);
    }

    /**
     * Met à jour le label du pas actuel.
     */
    public void refreshStep() {
        stepLabel.setText("Pas : " + simulator.getStep());
    }

    /**
     * Rafraîchit tous les composants visuels.
     */
    private void refreshAll() {
        refreshStep();
        gridView.refresh();
        statsPanel.refresh();
    }

    /**
     * Ouvre une boîte de dialogue pour sauvegarder la grille en CSV.
     */
    private void saveGrid() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fc.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                GridFileManager.save(simulator.getGrid(), file.getAbsolutePath());
            } catch (IOException ex) {
                showError("Impossible de sauvegarder : " + ex.getMessage());
            }
        }
    }

    /**
     * Ouvre une boîte de dialogue pour charger une grille depuis un CSV.
     */
    private void loadGrid() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fc.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                Grid loaded = GridFileManager.load(
                    file.getAbsolutePath(), simulator.getConfig());
                simulator.setGrid(loaded);
                refreshAll();
            } catch (IOException ex) {
                showError("Impossible de charger : " + ex.getMessage());
            }
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     *
     * @param message le message à afficher
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}