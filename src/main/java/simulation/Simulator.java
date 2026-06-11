package simulation;

import simulation.algorithme.OrthogonalSpread;
import simulation.algorithme.SpreadAlgorithm;
import simulation.chargeur.GridLoader;
import simulation.export.SimulationSnapshot;
import simulation.modele.Cell;
import simulation.modele.CellState;
import simulation.modele.Grid;
import simulation.modele.TerrainType;
import simulation.modele.Wind;
import simulation.serialisation.BinarySerializer;
import simulation.stats.GridStats;
import simulation.utilitaire.Constants;
import simulation.utilitaire.PhysicsFormulas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Central controller of the forest-fire simulation.
 *
 * <p>Pure model class — no UI dependency. The JavaFX application
 * drives this via a {@link javafx.animation.AnimationTimer} and calls
 * {@link #setOnRefresh(Consumer)} to receive update notifications.</p>
 */
public class Simulator {

    private Grid grid;
    private Grid initialGrid;
    private SpreadAlgorithm algorithm;
    private Wind wind;
    private int speedMs;
    private int step;
    private boolean simulationStarted;
    private final List<SimulationSnapshot> history;
    private Consumer<Grid> onRefresh;

    private static final Random RNG = new Random();

    /** Initialises with default settings and a random grid. */
    public Simulator() {
        speedMs           = Constants.DEFAULT_SPEED_MS;
        step              = 0;
        algorithm         = new OrthogonalSpread();
        wind              = new Wind();
        grid              = GridLoader.generateRandom();
        initialGrid       = copyGrid(grid);
        simulationStarted = false;
        history           = new ArrayList<>();
    }

    // ── Refresh callback ──────────────────────────────────────────────────────

    /**
     * Sets a callback invoked after every grid update (step, ignite, load…).
     * The JavaFX view registers here to redraw the canvas.
     *
     * @param callback consumer receiving the updated grid
     */
    public void setOnRefresh(Consumer<Grid> callback) {
        this.onRefresh = callback;
    }

    private void notifyRefresh() {
        if (onRefresh != null) onRefresh.accept(grid);
    }

    // ── Simulation control ────────────────────────────────────────────────────

    /**
     * Records the initial snapshot the first time the simulation starts.
     * Called by the JavaFX timer before the first step.
     */
    public void prepareStart() {
        if (!simulationStarted) {
            history.clear();
            recordSnapshot();
            simulationStarted = true;
        }
    }

    /** Resets to initial state. */
    public void reset() {
        step              = 0;
        grid              = copyGrid(initialGrid);
        simulationStarted = false;
        history.clear();
        notifyRefresh();
    }

    /**
     * Advances exactly one time step (step-by-step mode).
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
     * Advances the simulation by one time step (double-buffered).
     * Called by the JavaFX AnimationTimer every tick.
     */
    public void timeStep() {
        step++;
        Grid next = copyGrid(grid);

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell current = grid.getCell(x, y);
                if (current.getState() != CellState.ON_FIRE) continue;

                Cell currentNext = next.getCell(x, y);

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
     * Ignites the cell at (x, y) if possible.
     *
     * @param x column
     * @param y row
     */
    public void igniteCell(int x, int y) {
        Cell c = grid.getCell(x, y);
        if (c != null && c.canIgnite()) {
            c.setState(CellState.ON_FIRE);
            c.setBurnCounter(PhysicsFormulas.burnDuration(c));
            notifyRefresh();
        }
    }

    /**
     * Paints a rectangular zone with the given terrain type.
     *
     * @param x1      top-left column
     * @param y1      top-left row
     * @param x2      bottom-right column
     * @param y2      bottom-right row
     * @param terrain terrain to apply
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
                        case WATER      -> { c.setHumidity(1.0); c.setInflammability(0.0); }
                        case FIREBREAK  -> c.setInflammability(0.0);
                        case WET_ZONE   -> { c.setHumidity(0.8); c.setInflammability(0.1); }
                        case URBAN_ZONE -> c.setInflammability(0.0);
                        default         -> {}
                    }
                }
            }
        }
        notifyRefresh();
    }

    /**
     * Loads a new grid, resetting simulation state.
     *
     * @param newGrid grid to load
     */
    public void loadGrid(Grid newGrid) {
        step              = 0;
        grid              = newGrid;
        initialGrid       = copyGrid(grid);
        simulationStarted = false;
        history.clear();
        notifyRefresh();
    }

    // ── Binary save / load ────────────────────────────────────────────────────

    /**
     * Saves current state to a binary file.
     *
     * @param path output path
     * @throws IOException on write failure
     */
    public void saveBinary(String path) throws IOException {
        BinarySerializer.save(grid, wind, path);
    }

    /**
     * Loads state from a binary file.
     *
     * @param path input path
     * @throws IOException on read failure
     */
    public void loadBinary(String path) throws IOException {
        BinarySerializer.SavedState state = BinarySerializer.load(path);
        loadGrid(state.grid);
        setWind(state.wind);
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
}
