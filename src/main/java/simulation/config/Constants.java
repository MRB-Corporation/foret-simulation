package simulation.config;

/**
 * Central place for all simulation constants.
 *
 * <p>Fields marked as non-final are adjustable at runtime via the
 * parameter sliders in the control bar.</p>
 *
 * <p><b>Principe des curseurs (option B) :</b> chaque curseur règle la valeur
 * minimale d'un attribut de forêt (ex: humidité min). Une petite plage fixe
 * ({@code _RANGE}) est ajoutée par-dessus pour garder un peu de variété
 * aléatoire entre les cellules.</p>
 */
public final class Constants {

    private Constants() {}

    // ── Display ───────────────────────────────────────────────────────────────

    /** Default pixel size of one grid cell. */
    public static final int CELL_SIZE_PX = 12;

    // ── Simulation speed ──────────────────────────────────────────────────────

    public static final int DEFAULT_GRID_WIDTH  = 700;
    public static final int DEFAULT_GRID_HEIGHT = 700;

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
    //
    // Chaque attribut de forêt est généré ainsi :
    //     valeur = _MIN + random(0..1) × _RANGE
    //
    // Les curseurs de l'interface déplacent la valeur _MIN.
    // Le _RANGE reste fixe et petit pour garder une légère variété naturelle.

    /** Minimum initial temperature (°C) — slider-controlled. */
    public static double INIT_TEMP_MIN = 20.0;

    /** Maximum initial temperature (°C) — slider-controlled. */
    public static double INIT_TEMP_MAX = 30.0;

    /** Forest: minimum humidity — slider-controlled. */
    public static double FOREST_HUMIDITY_MIN = 0.35;

    /** Forest: fixed random humidity variation added on top of the minimum. */
    public static double FOREST_HUMIDITY_RANGE = 0.10;

    /** Forest: minimum inflammability — slider-controlled. */
    public static double FOREST_INFLAM_MIN = 0.55;

    /** Forest: fixed random inflammability variation added on top of the minimum. */
    public static double FOREST_INFLAM_RANGE = 0.10;

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