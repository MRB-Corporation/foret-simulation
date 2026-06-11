package simulation.utilitaire;

/**
 * Central place for all simulation constants.
 *
 * <p>Fields marked as non-final are adjustable at runtime via the
 * parameter-range sliders in the control bar.</p>
 */
public final class Constants {

    private Constants() {}

    // ── Display ───────────────────────────────────────────────────────────────

    /** Default pixel size of one grid cell. */
    public static final int CELL_SIZE_PX = 12;

    // ── Simulation speed ──────────────────────────────────────────────────────

    /** Default delay between two time steps (ms). */
    public static final int DEFAULT_SPEED_MS = 500;

    /** Minimum delay (fastest simulation). */
    public static final int MIN_SPEED_MS = 50;

    /** Maximum delay (slowest simulation). */
    public static final int MAX_SPEED_MS = 2000;

    // ── Fire / combustion ─────────────────────────────────────────────────────

    /** Base number of time steps a burning cell stays on fire. */
    public static final int BASE_BURN_DURATION = 15;

    /** Temperature (°C) of a burning cell. */
    public static final double FIRE_TEMPERATURE = 600.0;

    /** Heat conductivity fraction per step. */
    public static final double HEAT_CONDUCTIVITY = 0.1;

    /** Humidity drop per step when next to a burning cell. */
    public static final double HUMIDITY_DROP_PER_STEP = 0.02;

    // ── Initial cell attribute ranges (adjustable at runtime) ─────────────────

    /** Minimum initial temperature (°C). */
    public static double INIT_TEMP_MIN = 15.0;

    /** Maximum initial temperature (°C). */
    public static double INIT_TEMP_MAX = 35.0;

    /** Forest: minimum humidity. */
    public static double FOREST_HUMIDITY_MIN = 0.2;

    /** Forest: random humidity range. */
    public static double FOREST_HUMIDITY_RANGE = 0.4;

    /** Forest: minimum inflammability. */
    public static double FOREST_INFLAM_MIN = 0.4;

    /** Forest: random inflammability range. */
    public static double FOREST_INFLAM_RANGE = 0.4;

    /** Forest: minimum resistance. */
    public static double FOREST_RESISTANCE_MIN = 0.2;

    /** Forest: random resistance range. */
    public static double FOREST_RESISTANCE_RANGE = 0.3;

    /** Forest: minimum vegetation density. */
    public static double FOREST_DENSITY_MIN = 0.5;

    /** Forest: random vegetation density range. */
    public static double FOREST_DENSITY_RANGE = 0.5;

    /** Wet zone: fixed humidity. */
    public static final double WET_ZONE_HUMIDITY = 0.8;

    /** Wet zone: fixed inflammability. */
    public static final double WET_ZONE_INFLAM = 0.1;

    // ── Terrain type probabilities ─────────────────────────────────────────────

    /** Probability a random cell becomes water. */
    public static final double PROB_WATER    = 0.05;

    /** Probability a random cell becomes a firebreak. */
    public static final double PROB_FIREBREAK = 0.10;

    /** Probability a random cell becomes a wet zone. */
    public static final double PROB_WET_ZONE  = 0.15;

    /** Probability a random cell becomes urban. */
    public static final double PROB_URBAN     = 0.18;
}
