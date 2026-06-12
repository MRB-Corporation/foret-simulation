package simulation;

import config.SimulationConfig;
import model.Cell;
import model.CellState;
import model.Grid;

/**
 * Moteur principal de la simulation.
 * Gère l'avancement pas à pas, le démarrage, la pause,
 * la réinitialisation et l'allumage manuel du feu.
 */
public class Simulator {

    /** La grille de simulation. */
    private Grid grid;

    /** La configuration. */
    private final SimulationConfig config;

    /** L'algorithme de propagation. */
    private final FirePropagation propagation;

    /** Numéro du pas actuel. */
    private int step;

    /** Indique si la simulation tourne. */
    private boolean running;

    /**
     * Crée un simulateur avec une configuration donnée.
     *
     * @param config configuration de la simulation
     */
    public Simulator(SimulationConfig config) {
        this.config      = config;
        this.propagation = new FirePropagation();
        this.step        = 0;
        this.running     = false;
        this.grid        = new Grid(config);
    }

    /**
     * Avance la simulation d'un pas (si elle est en marche).
     */
    public void nextStep() {
        if (!running) return;
        step++;
        propagation.propagate(grid, config);
    }

    /**
     * Avance d'un seul pas même en pause (mode pas-à-pas).
     */
    public void singleStep() {
        step++;
        propagation.propagate(grid, config);
    }

    /** Démarre la simulation. */
    public void start() {
        running = true;
    }

    /** Met la simulation en pause. */
    public void pause() {
        running = false;
    }

    /**
     * Réinitialise la simulation avec une nouvelle grille aléatoire.
     */
    public void reset() {
        running = false;
        step    = 0;
        grid    = new Grid(config);
    }

    /**
     * Allume le feu sur la cellule (x, y) si elle peut brûler.
     *
     * @param x colonne
     * @param y ligne
     */
    public void igniteCell(int x, int y) {
        Cell cell = grid.getCell(x, y);
        if (cell != null && cell.canIgnite()) {
            cell.ignite(config);
        }
    }

    /**
     * Indique si la simulation est terminée
     * (plus aucune cellule en feu).
     *
     * @return true si aucune cellule ne brûle
     */
    public boolean isFinished() {
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                if (grid.getCell(x, y).getState() == CellState.BURNING) {
                    return false;
                }
            }
        }
        return true;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    /** @return la grille actuelle */
    public Grid getGrid() { return grid; }

    /** @param grid nouvelle grille (utilisé par le chargement de fichier) */
    public void setGrid(Grid grid) {
        this.grid = grid;
        this.step = 0;
        this.running = false;
    }

    /** @return le numéro du pas actuel */
    public int getStep() { return step; }

    /** @return true si la simulation est en marche */
    public boolean isRunning() { return running; }

    /** @return la configuration */
    public SimulationConfig getConfig() { return config; }
}