package simulation.services;

import simulation.services.OrthogonalSpread;
import simulation.services.SpreadAlgorithm;
import simulation.dao.GridLoader;
import simulation.entities.SimulationSnapshot;
import simulation.entities.Cell;
import simulation.entities.CellState;
import simulation.entities.Grid;
import simulation.entities.TerrainType;
import simulation.entities.Wind;
import simulation.entities.GridStats;
import simulation.entities.SavedState;
import simulation.entities.Environment;
import simulation.dao.BinarySerializer;
import simulation.config.Constants;
import simulation.utils.PhysicsFormulas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Contrôleur central de la simulation de feu de forêt (Le 'C' de l'architecture MVC).
 *
 * <p>Cette classe gère uniquement la logique du Modèle et ne contient aucune dépendance 
 * vers l'interface graphique (JavaFX). L'interface graphique pilote ce Simulator via un 
 * {@link javafx.animation.AnimationTimer} et s'abonne aux mises à jour via {@link #setOnRefresh(Consumer)}.</p>
 */
public class Simulator {

    private Grid grid;
    private Grid initialGrid;
    private SpreadAlgorithm algorithm;
    private Wind wind;
    private Environment environment;
    private int speedMs;
    private int step;
    private boolean simulationStarted;
    private final List<SimulationSnapshot> history;
    private Consumer<Grid> onRefresh;

    private static final Random RNG = new Random();

    /** 
     * Constructeur par défaut : initialise les paramètres de base et génère une grille aléatoire. 
     */
    public Simulator() {
        speedMs           = Constants.DEFAULT_SPEED_MS;
        step              = 0;
        algorithm         = new OrthogonalSpread();
        wind              = new Wind();
        environment       = new Environment();
        grid              = GridLoader.generateRandom();
        initialGrid       = copyGrid(grid);
        simulationStarted = false;
        history           = new ArrayList<>();
    }

    // ── Refresh callback ──────────────────────────────────────────────────────

    /**
     * Définit le "Callback" (la fonction de rappel) qui sera exécutée après chaque 
     * mise à jour de la grille (avancée d'un "tick", clic de l'utilisateur, chargement...).
     * 
     * <p>C'est par ce biais que la Vue (JavaFX) est prévenue qu'elle doit se redessiner, 
     * garantissant ainsi une étanchéité parfaite entre le calcul et l'affichage.</p>
     *
     * @param callback La fonction acceptant la nouvelle grille.
     */
    public void setOnRefresh(Consumer<Grid> callback) {
        this.onRefresh = callback;
    }

    private void notifyRefresh() {
        if (onRefresh != null) onRefresh.accept(grid);
    }

    // ── Simulation control ────────────────────────────────────────────────────

    /**
     * Enregistre l'état initial (Snapshot) la toute première fois que la simulation démarre.
     * Appelée par le timer JavaFX juste avant le premier calcul.
     */
    public void prepareStart() {
        if (!simulationStarted) {
            history.clear();
            recordSnapshot();
            simulationStarted = true;
        }
    }

    /** Réinitialise la simulation à son état de départ exact. */
    public void reset() {
        step              = 0;
        grid              = copyGrid(initialGrid);
        environment.applyTo(grid);
        simulationStarted = false;
        history.clear();
        notifyRefresh();
    }

    /**
     * Fait avancer la simulation d'exactement un seul pas de temps (Mode Pas-à-Pas).
     */
    public void stepOnce() {
        if (!simulationStarted) {
            history.clear();
            recordSnapshot();
            simulationStarted = true;
        }
        timeStep();
        notifyRefresh();
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    /**
     * Fonction maîtresse : Fait avancer la simulation d'une unité de temps.
     * 
     * <p>Nous avons implémenté ici le mécanisme de "Double Buffering" :
     * On lit l'état de la grille courante (`grid`), on calcule les propagations, 
     * et on écrit les résultats dans une TOUTE NOUVELLE grille (`next`).
     * Cela évite le bug classique de l'automate cellulaire où un feu pourrait
     * traverser toute la carte en une seule itération à cause de l'ordre de parcours
     * de la boucle 'for'.</p>
     */
    public void timeStep() {
        step++;
        Grid next = copyGrid(grid);

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell current = grid.getCell(x, y);
                Cell currentNext = next.getCell(x, y);

                if (current.getState() != CellState.ON_FIRE) {
                    if (currentNext.getTemperature() > Constants.INIT_TEMP_MIN) {
                        currentNext.setTemperature(Math.max(Constants.INIT_TEMP_MIN, currentNext.getTemperature() - 5.0));
                    }
                    continue;
                }

                // Update neighbours before propagating
                for (Cell nb : algorithm.getNeighbours(current, grid)) {
                    Cell nbNext = next.getCell(nb.getX(), nb.getY());
                    nbNext.heat(PhysicsFormulas.temperatureDelta(nb));
                    nbNext.adjustHumidity(-Constants.HUMIDITY_DROP_PER_STEP);
                }

                // Propagate fire
                for (Cell candidate : algorithm.getCandidates(current, grid)) {
                    int dx = candidate.getX() - current.getX();
                    int dy = candidate.getY() - current.getY();
                    Cell candidateNext = next.getCell(candidate.getX(), candidate.getY());
                    double prob = PhysicsFormulas.ignitionProbabilityWithWind(candidate, wind, dx, dy);
                    if (RNG.nextDouble() < prob && candidate.canIgnite()) {
                        candidateNext.setState(CellState.ON_FIRE);
                        candidateNext.setTemperature(Constants.FIRE_TEMPERATURE);
                        candidateNext.setBurnCounter(PhysicsFormulas.burnDuration(candidate));
                    }
                }

                // Burn counter
                currentNext.decrementBurnCounter();
                if (currentNext.getBurnCounter() <= 0)
                    currentNext.setState(CellState.BURNED);
            }
        }

        grid = next;
        if (simulationStarted) recordSnapshot();
    }

    // ── User interaction ──────────────────────────────────────────────────────

    /**
     * Enflamme manuellement la cellule aux coordonnées (x, y). (Interaction Utilisateur)
     *
     * @param x Colonne de la grille ciblée
     * @param y Ligne de la grille ciblée
     */
    public void igniteCell(int x, int y) {
        Cell c = grid.getCell(x, y);
        if (c != null && c.canIgnite()) {
            c.setState(CellState.ON_FIRE);
            c.setTemperature(Constants.FIRE_TEMPERATURE);
            c.setBurnCounter(PhysicsFormulas.burnDuration(c));
            notifyRefresh();
        }
    }

    /**
     * Dessine (ou "peint") une zone rectangulaire avec un type de terrain spécifique.
     * Utilisé par l'outil Pinceau de l'interface pour créer des lacs ou des pare-feux en direct.
     *
     * @param x1      Colonne du coin supérieur gauche
     * @param y1      Ligne du coin supérieur gauche
     * @param x2      Colonne du coin inférieur droit
     * @param y2      Ligne du coin inférieur droit
     * @param terrain Le type de terrain à appliquer
     */
    public void paintZone(int x1, int y1, int x2, int y2, TerrainType terrain) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Cell c = grid.getCell(x, y);
                if (c != null) {
                    c.setTerrain(terrain);
                    c.setState(CellState.INTACT);
                    switch (terrain) {
                        case WATER      -> { c.setHumidity(1.0); c.setInflammability(0.0); c.setVegetationDensity(0.0); }
                        case FIREBREAK  -> { c.setInflammability(0.0); c.setVegetationDensity(0.0); }
                        case WET_ZONE   -> { c.setHumidity(0.8); c.setInflammability(0.1); c.setVegetationDensity(0.8); }
                        case URBAN_ZONE -> { c.setInflammability(0.0); c.setVegetationDensity(0.2); }
                        default         -> { 
                            // FOREST
                            c.setHumidity(Constants.FOREST_HUMIDITY_MIN + RNG.nextDouble() * Constants.FOREST_HUMIDITY_RANGE);
                            c.setInflammability(Constants.FOREST_INFLAM_MIN + RNG.nextDouble() * Constants.FOREST_INFLAM_RANGE);
                            c.setVegetationDensity(Constants.FOREST_DENSITY_MIN + RNG.nextDouble() * Constants.FOREST_DENSITY_RANGE);
                        }
                    }
                }
            }
        }
        notifyRefresh();
    }

    /**
     * Charge une toute nouvelle grille et réinitialise le compteur de la simulation.
     *
     * @param newGrid La nouvelle grille pré-générée à charger.
     */
    public void loadGrid(Grid newGrid) {
        step              = 0;
        grid              = newGrid;
        initialGrid       = copyGrid(grid);
        environment.applyTo(grid);
        simulationStarted = false;
        history.clear();
        notifyRefresh();
    }

    // ── Binary save / load ────────────────────────────────────────────────────

    /**
     * Sauvegarde l'état actuel complet de la simulation dans un fichier binaire sérialisé.
     *
     * @param path Chemin de destination du fichier (.ffsave)
     * @throws IOException Si l'écriture sur le disque échoue
     */
    public void saveBinary(String path) throws IOException {
        BinarySerializer.save(grid, wind, path);
    }

    /**
     * Restaure l'état d'une simulation depuis un fichier binaire.
     *
     * @param path Chemin source du fichier
     * @throws IOException Si la lecture du disque échoue
     */
    public void loadBinary(String path) throws IOException {
        SavedState state = BinarySerializer.load(path);
        loadGrid(state.grid);
        setWind(state.wind);
        environment.applyTo(grid);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** @return live statistics for the current grid */
    public GridStats computeStats() {
        return GridStats.compute(grid, step);
    }

    private Grid copyGrid(Grid source) {
        Grid copy = new Grid(source.getWidth(), source.getHeight(), source.isToroidal());
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Cell src = source.getCell(x, y);
                Cell dst = copy.getCell(x, y);
                dst.setState(src.getState());
                dst.setTerrain(src.getTerrain());
                dst.setHumidity(src.getHumidity());
                dst.setInflammability(src.getInflammability());
                dst.setResistance(src.getResistance());
                dst.setVegetationDensity(src.getVegetationDensity());
                dst.setTemperature(src.getTemperature());
                dst.setBurnCounter(src.getBurnCounter());
            }
        }
        return copy;
    }

    private void recordSnapshot() {
        history.add(SimulationSnapshot.from(grid, step, algorithm.getName(), wind));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /** @return current grid */
    public Grid getGrid()  { return grid; }

    /** @return current step */
    public int getStep()   { return step; }

    /** @return current wind */
    public Wind getWind()  { return wind; }
    
    /** @return current environment */
    public Environment getEnvironment() { return environment; }

    /** @return simulation speed in ms */
    public int getSpeedMs() { return speedMs; }

    /** @return unmodifiable history */
    public List<SimulationSnapshot> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** @param algorithm new spread algorithm */
    public void setAlgorithm(SpreadAlgorithm algorithm) { this.algorithm = algorithm; }

    /** @param ms new delay between steps */
    public void setSpeed(int ms) { this.speedMs = ms; }

    /** @param wind new wind */
    public void setWind(Wind wind) { this.wind = wind; }

    /** @param toroidal topology mode */
    public void setToroidal(boolean toroidal) { grid.setToroidal(toroidal); }

    /** Re-applies the environment immediately to the current grid */
    public void applyEnvironment() {
        environment.applyTo(grid);
        notifyRefresh();
    }
}
