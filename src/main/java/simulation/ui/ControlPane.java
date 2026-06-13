package simulation.ui;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import simulation.services.Simulator;
import simulation.services.MooreSpread;
import simulation.services.OrthogonalSpread;
import simulation.services.RadialSpread;
import simulation.services.SpreadAlgorithm;
import simulation.dao.GridLoader;
import simulation.dao.PredefinedTopologies;
import simulation.dao.SimulationCsvExporter;
import simulation.entities.GridStats;
import simulation.entities.Grid;
import simulation.entities.Environment;
import simulation.entities.TerrainType;
import simulation.entities.Wind;
import simulation.dao.BinarySerializer;
import simulation.config.Constants;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX control bar displayed at the bottom of the main window.
 *
 * <p>Row 1 — simulation: Start/Pause, Step, Reset, speed slider, algorithm, wind.</p>
 * <p>Row 2 — interaction: topology, toroidal, paint terrain, zoom, files.</p>
 * <p>Row 3 — parameter ranges: humidity, inflammability, temperature sliders.</p>
 */
public class ControlPane extends VBox {

    private static final SpreadAlgorithm[] ALGORITHMS = {
        new OrthogonalSpread(), new MooreSpread(), new RadialSpread()
    };
    private static final double[] WIND_DIRS = {
        0.0, Math.PI / 2, -Math.PI / 2, 0.0, Math.PI
    };

    private final Simulator   simulator;
    private final GridCanvas  canvas;
    private final StatsPane   statsPane;

    private final Button btnStartPause = new Button("▶ Start");
    private final Label  lblStep       = new Label("Step: 0");
    private boolean      running       = false;
    private AnimationTimer timer;
    private long lastStepTime = 0;

    /**
     * Builds the full control bar.
     *
     * @param simulator the simulator to control
     * @param canvas    the grid canvas (zoom delegation)
     * @param statsPane the stats panel (updated on each step)
     */
    public ControlPane(Simulator simulator, GridCanvas canvas, StatsPane statsPane) {
        this.simulator = simulator;
        this.canvas    = canvas;
        this.statsPane = statsPane;

        setSpacing(2);
        setPadding(new Insets(4, 6, 4, 6));
        setStyle("-fx-background-color: #ececec;");

        getChildren().addAll(buildRow1(), buildRow2(), buildRow3());
        buildTimer();
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void buildTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long delayNs = (long) simulator.getSpeedMs() * 1_000_000L;
                if (now - lastStepTime >= delayNs) {
                    lastStepTime = now;
                    simulator.timeStep();
                    canvas.drawGrid(simulator.getGrid());
                    
                    GridStats stats = simulator.computeStats();
                    statsPane.update(stats);
                    lblStep.setText("Step: " + simulator.getStep());
                    
                    if (stats.getBurningCells() == 0 && simulator.getStep() > 0) {
                        stopTimer();
                    }
                }
            }
        };
    }

    // ── Row 1: simulation controls ────────────────────────────────────────────

    private FlowPane buildRow1() {
        FlowPane row = row();

        // Start / Pause
        btnStartPause.setOnAction(e -> toggleStartPause());
        row.getChildren().add(btnStartPause);

        // Step once
        Button btnStep = new Button("⏭ Step");
        btnStep.setTooltip(new Tooltip("Advance one step"));
        btnStep.setOnAction(e -> {
            stopTimer();
            simulator.stepOnce();
            lblStep.setText("Step: " + simulator.getStep());
            statsPane.update(simulator.computeStats());
        });
        row.getChildren().add(btnStep);

        // Reset
        Button btnReset = new Button("⏹ Reset");
        btnReset.setOnAction(e -> {
            stopTimer();
            simulator.reset();
            lblStep.setText("Step: 0");
            canvas.drawGrid(simulator.getGrid());
            statsPane.update(simulator.computeStats());
        });
        row.getChildren().add(btnReset);

        // New Random
        Button btnRandom = new Button("🔀 New Random");
        btnRandom.setOnAction(e -> {
            stopTimer();
            simulator.loadGrid(GridLoader.generateRandom());
            lblStep.setText("Step: 0");
            canvas.resetZoom();
            statsPane.update(simulator.computeStats());
        });
        row.getChildren().add(btnRandom);

        row.getChildren().add(sep());

        // Speed
        row.getChildren().add(new Label("Speed:"));
        Slider sliderSpeed = new Slider(Constants.MIN_SPEED_MS, Constants.MAX_SPEED_MS,
                Constants.DEFAULT_SPEED_MS);
        sliderSpeed.setOrientation(Orientation.HORIZONTAL);
        sliderSpeed.setPrefWidth(120);
        sliderSpeed.valueProperty().addListener((o, old, val) ->
                simulator.setSpeed(val.intValue()));
        row.getChildren().add(sliderSpeed);

        row.getChildren().add(sep());

        // Algorithm
        row.getChildren().add(new Label("Algorithm:"));
        ComboBox<String> comboAlgo = new ComboBox<>();
        for (SpreadAlgorithm a : ALGORITHMS) comboAlgo.getItems().add(a.getName());
        comboAlgo.getSelectionModel().selectFirst();
        comboAlgo.setOnAction(e ->
                simulator.setAlgorithm(ALGORITHMS[comboAlgo.getSelectionModel().getSelectedIndex()]));
        row.getChildren().add(comboAlgo);

        row.getChildren().add(sep());

        // Wind
        row.getChildren().add(new Label("Wind:"));
        ComboBox<String> comboWind = new ComboBox<>();
        comboWind.getItems().addAll("None","From North","From South","From West","From East");
        comboWind.getSelectionModel().selectFirst();
        Slider sliderWind = new Slider(0, 100, 0);
        sliderWind.setPrefWidth(90);
        Runnable applyWind = () -> {
            int idx = comboWind.getSelectionModel().getSelectedIndex();
            double spd = idx == 0 ? 0.0 : sliderWind.getValue() / 100.0;
            simulator.setWind(new Wind(WIND_DIRS[idx], spd));
        };
        comboWind.setOnAction(e -> applyWind.run());
        sliderWind.valueProperty().addListener((o, old, val) -> applyWind.run());
        row.getChildren().addAll(comboWind, sliderWind);

        row.getChildren().add(sep());
        row.getChildren().add(lblStep);

        return row;
    }

    // ── Row 2: grid / IO controls ─────────────────────────────────────────────

    private FlowPane buildRow2() {
        FlowPane row = row();

        // Topology presets
        row.getChildren().add(new Label("Topology:"));
        ComboBox<String> comboTopo = new ComboBox<>();
        comboTopo.getItems().addAll("— choose —","Forêt Amazonienne","Forêt des Landes","Forêt Boréale","Brousse Australienne");
        comboTopo.getSelectionModel().selectFirst();
        comboTopo.setOnAction(e -> {
            int idx = comboTopo.getSelectionModel().getSelectedIndex();
            if (idx <= 0) return;
            Grid g = switch (idx) {
                case 1  -> PredefinedTopologies.amazonie();
                case 2  -> PredefinedTopologies.foretDesLandes();
                case 3  -> PredefinedTopologies.foretBoreale();
                default -> PredefinedTopologies.brousseAustralienne();
            };
            stopTimer();
            simulator.loadGrid(g);
            lblStep.setText("Step: 0");
            comboTopo.getSelectionModel().selectFirst();
        });
        row.getChildren().add(comboTopo);

        // Toroidal checkbox
        CheckBox chkToroidal = new CheckBox("Toroidal");
        chkToroidal.setTooltip(new Tooltip("Wrap grid edges"));
        chkToroidal.setOnAction(e -> simulator.setToroidal(chkToroidal.isSelected()));
        row.getChildren().add(chkToroidal);

        row.getChildren().add(sep());

        // Paint terrain
        row.getChildren().add(new Label("Paint:"));
        ComboBox<String> comboPaint = new ComboBox<>();
        comboPaint.getItems().addAll("Forest","Water","Wet Zone","Firebreak","Urban");
        comboPaint.getSelectionModel().selectFirst();
        comboPaint.setOnAction(e -> {
            TerrainType t = switch (comboPaint.getSelectionModel().getSelectedIndex()) {
                case 1  -> TerrainType.WATER;
                case 2  -> TerrainType.WET_ZONE;
                case 3  -> TerrainType.FIREBREAK;
                case 4  -> TerrainType.URBAN_ZONE;
                default -> TerrainType.FOREST;
            };
            canvas.setPaintTerrain(t);
        });
        row.getChildren().add(comboPaint);

        row.getChildren().add(sep());

        // Zoom
        Button btnZoomIn    = new Button("🔍+");
        Button btnZoomOut   = new Button("🔍-");
        Button btnZoomReset = new Button("⊙");
        btnZoomIn.setOnAction(e    -> canvas.zoomIn());
        btnZoomOut.setOnAction(e   -> canvas.zoomOut());
        btnZoomReset.setOnAction(e -> canvas.resetZoom());
        row.getChildren().addAll(btnZoomIn, btnZoomOut, btnZoomReset);

        row.getChildren().add(sep());

        // File IO
        Button btnLoadCsv = new Button("Load CSV");
        Button btnSaveCsv = new Button("Save CSV");
        Button btnSaveBin = new Button("Save .ffsave");
        Button btnLoadBin = new Button("Load .ffsave");
        Button btnExport  = new Button("Export Stats");

        btnLoadCsv.setOnAction(e -> loadCsv());
        btnSaveCsv.setOnAction(e -> saveCsv());
        btnSaveBin.setOnAction(e -> saveBinary());
        btnLoadBin.setOnAction(e -> loadBinary());
        btnExport.setOnAction(e  -> exportStats());

        row.getChildren().addAll(btnLoadCsv, btnSaveCsv, btnSaveBin, btnLoadBin, btnExport);

        return row;
    }

/**
     * Row 3 : environnement + curseurs de paramètres de forêt .
     *
     * <p>Chaque curseur règle la valeur MINIMALE d'un attribut, et affiche sa
     * valeur en direct. Une petite variation aléatoire fixe ({@code _RANGE})
     * est ajoutée automatiquement à la génération pour garder de la variété.
     * Les nouvelles valeurs s'appliquent au prochain "Reset" ou "New Random".</p>
     */
    private FlowPane buildRow3() {
        FlowPane row = row();
        row.getChildren().add(new Label("🌦 Environment:"));

        ComboBox<Environment.Season> comboSeason = new ComboBox<>();
        comboSeason.getItems().addAll(Environment.Season.values());
        comboSeason.getSelectionModel().select(simulator.getEnvironment().getSeason());
        comboSeason.setOnAction(e -> {
            simulator.getEnvironment().setSeason(comboSeason.getValue());
            simulator.applyEnvironment();
        });

        ComboBox<Environment.Sunlight> comboSunlight = new ComboBox<>();
        comboSunlight.getItems().addAll(Environment.Sunlight.values());
        comboSunlight.getSelectionModel().select(simulator.getEnvironment().getSunlight());
        comboSunlight.setOnAction(e -> {
            simulator.getEnvironment().setSunlight(comboSunlight.getValue());
            simulator.applyEnvironment();
        });

        row.getChildren().addAll(comboSeason, comboSunlight, sep());

        row.getChildren().add(new Label("🌲 Forest params:"));

        // ── Humidité (curseur unique : règle le minimum) ──────────────────────
        Label lblHum = new Label(String.format("Humidity: %.2f", Constants.FOREST_HUMIDITY_MIN));
        Slider sHum = rangeSlider(0.0, 1.0, Constants.FOREST_HUMIDITY_MIN);
        sHum.valueProperty().addListener((o, old, v) -> {
            Constants.FOREST_HUMIDITY_MIN = v.doubleValue();
            lblHum.setText(String.format("Humidity: %.2f", v.doubleValue()));
        });
        row.getChildren().addAll(lblHum, sHum, sep());

        // ── Inflammabilité (curseur unique : règle le minimum) ────────────────
        Label lblInflam = new Label(String.format("Inflammability: %.2f", Constants.FOREST_INFLAM_MIN));
        Slider sInflam = rangeSlider(0.0, 1.0, Constants.FOREST_INFLAM_MIN);
        sInflam.valueProperty().addListener((o, old, v) -> {
            Constants.FOREST_INFLAM_MIN = v.doubleValue();
            lblInflam.setText(String.format("Inflammability: %.2f", v.doubleValue()));
        });
        row.getChildren().addAll(lblInflam, sInflam, sep());

        // ── Température (deux curseurs : min et max, avec sécurité) ────────────
        Label lblTempMin = new Label(String.format("Temp min: %.0f°C", Constants.INIT_TEMP_MIN));
        Slider sTempMin = rangeSlider(0, 50, Constants.INIT_TEMP_MIN);
        sTempMin.valueProperty().addListener((o, old, v) -> {
            Constants.INIT_TEMP_MIN = v.doubleValue();
            // Sécurité : le min ne doit jamais dépasser le max
            if (Constants.INIT_TEMP_MIN > Constants.INIT_TEMP_MAX) {
                Constants.INIT_TEMP_MAX = Constants.INIT_TEMP_MIN;
            }
            lblTempMin.setText(String.format("Temp min: %.0f°C", Constants.INIT_TEMP_MIN));
        });

        Label lblTempMax = new Label(String.format("max: %.0f°C", Constants.INIT_TEMP_MAX));
        Slider sTempMax = rangeSlider(0, 50, Constants.INIT_TEMP_MAX);
        sTempMax.valueProperty().addListener((o, old, v) -> {
            Constants.INIT_TEMP_MAX = v.doubleValue();
            // Sécurité : le max ne doit jamais passer sous le min
            if (Constants.INIT_TEMP_MAX < Constants.INIT_TEMP_MIN) {
                Constants.INIT_TEMP_MIN = Constants.INIT_TEMP_MAX;
            }
            lblTempMax.setText(String.format("max: %.0f°C", Constants.INIT_TEMP_MAX));
        });

        row.getChildren().addAll(lblTempMin, sTempMin, lblTempMax, sTempMax);

        return row;
    }
    // ── Timer control ─────────────────────────────────────────────────────────

    private void toggleStartPause() {
        if (running) {
            stopTimer();
        } else {
            simulator.prepareStart();
            lastStepTime = 0;
            timer.start();
            running = true;
            btnStartPause.setText("⏸ Pause");
        }
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
        running = false;
        btnStartPause.setText("▶ Start");
    }

    // ── File helpers ──────────────────────────────────────────────────────────

    private void loadCsv() {
        File f = fileChooser("CSV", "*.csv").showOpenDialog(getScene().getWindow());
        if (f != null) {
            stopTimer();
            simulator.loadGrid(GridLoader.load(f.getAbsolutePath()));
            lblStep.setText("Step: 0");
        }
    }

    private void saveCsv() {
        File f = fileChooser("CSV", "*.csv").showSaveDialog(getScene().getWindow());
        if (f != null) {
            String path = ensureExt(f.getAbsolutePath(), ".csv");
            GridLoader.save(simulator.getGrid(), path);
        }
    }

    private void saveBinary() {
        File f = fileChooser("Simulation save", "*.ffsave").showSaveDialog(getScene().getWindow());
        if (f != null) {
            try {
                simulator.saveBinary(ensureExt(f.getAbsolutePath(), BinarySerializer.EXTENSION));
                alert("Saved", "File saved successfully.", Alert.AlertType.INFORMATION);
            } catch (IOException ex) {
                alert("Error", "Save failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadBinary() {
        File f = fileChooser("Simulation save", "*.ffsave").showOpenDialog(getScene().getWindow());
        if (f != null) {
            try {
                stopTimer();
                simulator.loadBinary(f.getAbsolutePath());
                lblStep.setText("Step: " + simulator.getStep());
            } catch (IOException ex) {
                alert("Error", "Load failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportStats() {
        if (simulator.getHistory().isEmpty()) {
            alert("No data", "Start the simulation first.", Alert.AlertType.INFORMATION);
            return;
        }
        File f = fileChooser("CSV", "*.csv").showSaveDialog(getScene().getWindow());
        if (f != null) {
            try {
                SimulationCsvExporter.export(simulator.getHistory(),
                        ensureExt(f.getAbsolutePath(), ".csv"));
                alert("Exported",
                        simulator.getHistory().size() + " rows exported.",
                        Alert.AlertType.INFORMATION);
            } catch (IOException ex) {
                alert("Error", "Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static FlowPane row() {
        FlowPane p = new FlowPane();
        p.setHgap(6);
        p.setVgap(2);
        p.setPadding(new Insets(2, 0, 2, 0));
        return p;
    }

    private static Separator sep() {
        Separator s = new Separator(Orientation.VERTICAL);
        s.setPrefHeight(20);
        return s;
    }

    private static Slider rangeSlider(double min, double max, double value) {
        Slider s = new Slider(min, max, value);
        s.setPrefWidth(80);
        s.setShowTickLabels(false);
        return s;
    }

    private static FileChooser fileChooser(String desc, String ext) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(desc + " (" + ext + ")", ext));
        return fc;
    }

    private static String ensureExt(String path, String ext) {
        return path.endsWith(ext) ? path : path + ext;
    }

    private static void alert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
