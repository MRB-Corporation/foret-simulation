package simulation.modele;

import simulation.utilitaire.Constants;

import java.util.Random;

/**
 * Represents a single cell of the simulation grid.
 *
 * <p>Each cell stores physical attributes (humidity, inflammability, resistance,
 * vegetation density, temperature) that drive the fire-spread formulas, as well
 * as its current {@link CellState} and {@link TerrainType}.</p>
 *
 * <p>Implements {@link FireBehaviour} so the propagation algorithm can interact
 * with cells through that interface alone.</p>
 */
public class Cell implements FireBehaviour {

    private static final Random RANDOM = new Random();

    // ── Position ──────────────────────────────────────────────────────────────

    private final int x;
    private final int y;

    // ── State & terrain ───────────────────────────────────────────────────────

    private CellState state;
    private TerrainType terrain;

    // ── Physical attributes (all in [0.0, 1.0] unless noted) ─────────────────

    /** Moisture level — high humidity slows ignition. */
    private double humidity;

    /** How easily this cell catches fire. */
    private double inflammability;

    /** How long this cell resists fire once ignited. */
    private double resistance;

    /** Amount of burnable vegetation — more density ⟹ longer burn. */
    private double vegetationDensity;

    /** Current temperature in °C — starts random in [INIT_TEMP_MIN, INIT_TEMP_MAX]. */
    private double temperature;

    /** Remaining time steps before the fire on this cell goes out. */
    private int burnCounter;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a cell at position (x, y) with default forest attributes and a
     * random initial temperature.
     *
     * @param x column index in the grid
     * @param y row index in the grid
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.INTACT;
        this.terrain = TerrainType.FOREST;
        this.humidity = 0.30;
        this.inflammability = 0.50;
        this.resistance = 0.50;
        this.vegetationDensity = 1.0;
        // Random temperature between INIT_TEMP_MIN and INIT_TEMP_MAX
        this.temperature = Constants.INIT_TEMP_MIN
                + RANDOM.nextDouble() * (Constants.INIT_TEMP_MAX - Constants.INIT_TEMP_MIN);
        this.burnCounter = 0;
    }

    // ── FireBehaviour ─────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>A cell can ignite if and only if it is {@link CellState#INTACT} and its
     * terrain does not block fire propagation.</p>
     */
    @Override
    public boolean canIgnite() {
        return state == CellState.INTACT && !blocksfire();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Water, firebreaks, wet zones and urban zones all block fire.</p>
     */
    @Override
    public boolean blocksfire() {
        return terrain == TerrainType.FIREBREAK
            || terrain == TerrainType.WATER
            || terrain == TerrainType.WET_ZONE
            || terrain == TerrainType.URBAN_ZONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void heat(double delta) {
        temperature += delta;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Result is clamped to [0.0, 1.0].</p>
     */
    @Override
    public void adjustHumidity(double delta) {
        humidity = Math.max(0.0, Math.min(1.0, humidity + delta));
    }

    // ── Combustion helpers ────────────────────────────────────────────────────

    /**
     * Decrements the burn counter by one.
     * Call once per time step while the cell is {@link CellState#ON_FIRE}.
     */
    public void decrementBurnCounter() {
        if (burnCounter > 0) burnCounter--;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return column index */
    public int getX() { return x; }

    /** @return row index */
    public int getY() { return y; }

    /** @return current fire state */
    public CellState getState() { return state; }

    /** @return terrain type */
    public TerrainType getTerrain() { return terrain; }

    /** @return humidity in [0.0, 1.0] */
    public double getHumidity() { return humidity; }

    /** @return inflammability in [0.0, 1.0] */
    public double getInflammability() { return inflammability; }

    /** @return resistance in [0.0, 1.0] */
    public double getResistance() { return resistance; }

    /** @return vegetation density in [0.0, 1.0] */
    public double getVegetationDensity() { return vegetationDensity; }

    /** @return temperature in °C */
    public double getTemperature() { return temperature; }

    /** @return remaining burn steps */
    public int getBurnCounter() { return burnCounter; }

    // ── Setters ───────────────────────────────────────────────────────────────

    /** @param state new fire state */
    public void setState(CellState state) { this.state = state; }

    /** @param terrain new terrain type */
    public void setTerrain(TerrainType terrain) { this.terrain = terrain; }

    /** @param v temperature in °C */
    public void setTemperature(double v) { this.temperature = v; }

    /** @param v remaining burn steps */
    public void setBurnCounter(int v) { this.burnCounter = v; }

    /**
     * Sets humidity, clamped to [0.0, 1.0].
     * @param v humidity value
     */
    public void setHumidity(double v) {
        humidity = Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Sets inflammability, clamped to [0.0, 1.0].
     * @param v inflammability value
     */
    public void setInflammability(double v) {
        inflammability = Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Sets resistance, clamped to [0.0, 1.0].
     * @param v resistance value
     */
    public void setResistance(double v) {
        resistance = Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Sets vegetation density, clamped to [0.0, 1.0].
     * @param v density value
     */
    public void setVegetationDensity(double v) {
        vegetationDensity = Math.max(0.0, Math.min(1.0, v));
    }
}
