package model;

import config.SimulationConfig;
import java.util.Random;

/**
 * Représente une cellule dans la grille.
 * Chaque cellule possède une position, un état, un terrain,
 * une humidité et une température générées aléatoirement
 * dans les plages définies par la configuration.
 */
public class Cell {

    /** Générateur aléatoire partagé par toutes les cellules. */
    private static final Random RANDOM = new Random();

    /** Colonne de la cellule. */
    private final int x;

    /** Ligne de la cellule. */
    private final int y;

    /** État actuel (INTACT, BURNING, BURNED). */
    private CellState state;

    /** Type de terrain. */
    private TerrainType terrain;

    /** Humidité entre 0.0 (sec) et 1.0 (très humide). */
    private double humidity;

    /** Température en °C. */
    private double temperature;

    /** Pas restants avant la fin de la combustion. */
    private int burnCountdown;

    /**
     * Crée une cellule avec des valeurs aléatoires dans les plages
     * de la configuration.
     *
     * @param x      colonne
     * @param y      ligne
     * @param config configuration de la simulation
     */
    public Cell(int x, int y, SimulationConfig config) {
        this.x = x;
        this.y = y;
        this.state = CellState.INTACT;
        this.burnCountdown = 0;

        // Humidité aléatoire entre humidityMin et humidityMax
        this.humidity = config.humidityMin
            + RANDOM.nextDouble() * (config.humidityMax - config.humidityMin);

        // Température aléatoire entre temperatureMin et temperatureMax
        this.temperature = config.temperatureMin
            + RANDOM.nextDouble() * (config.temperatureMax - config.temperatureMin);

        // Terrain aléatoire selon des probabilités fixes
        double draw = RANDOM.nextDouble();
        if      (draw < 0.05) this.terrain = TerrainType.WATER;
        else if (draw < 0.10) this.terrain = TerrainType.FIREBREAK;
        else if (draw < 0.15) this.terrain = TerrainType.WET_ZONE;
        else if (draw < 0.18) this.terrain = TerrainType.URBAN;
        else                  this.terrain = TerrainType.FOREST;
    }

    /**
     * Indique si la cellule peut s'enflammer.
     * Il faut être INTACT et avoir un terrain inflammable.
     *
     * @return true si la cellule peut prendre feu
     */
    public boolean canIgnite() {
        return state == CellState.INTACT
            && terrain != TerrainType.WATER
            && terrain != TerrainType.FIREBREAK
            && terrain != TerrainType.URBAN;
    }

    /**
     * Enflamme la cellule et initialise son compteur de combustion.
     *
     * @param config configuration de la simulation
     */
    public void ignite(SimulationConfig config) {
        this.state = CellState.BURNING;
        this.burnCountdown = config.baseBurnDuration;
    }

    /**
     * Avance la combustion d'un pas.
     * Quand le compteur atteint 0, la cellule devient BURNED.
     */
    public void burnStep() {
        if (state == CellState.BURNING) {
            burnCountdown--;
            if (burnCountdown <= 0) {
                state = CellState.BURNED;
            }
        }
    }

    /**
     * Baisse l'humidité (utilisé quand un voisin brûle).
     * L'humidité reste toujours entre 0.0 et 1.0.
     *
     * @param amount quantité à soustraire
     */
    public void decreaseHumidity(double amount) {
        humidity = Math.max(0.0, humidity - amount);
    }

    /**
     * Augmente la température (utilisé quand un voisin brûle).
     *
     * @param amount quantité à ajouter en °C
     */
    public void increaseTemperature(double amount) {
        temperature += amount;
    }

    // ── Getters ────────────────────────────────────────────────────────────

    /** @return la colonne */
    public int getX() { return x; }

    /** @return la ligne */
    public int getY() { return y; }

    /** @return l'état actuel */
    public CellState getState() { return state; }

    /** @return le terrain */
    public TerrainType getTerrain() { return terrain; }

    /** @return l'humidité (0.0 à 1.0) */
    public double getHumidity() { return humidity; }

    /** @return la température en °C */
    public double getTemperature() { return temperature; }

    // ── Setters ────────────────────────────────────────────────────────────

    /** @param state nouvel état */
    public void setState(CellState state) { this.state = state; }

    /** @param terrain nouveau terrain */
    public void setTerrain(TerrainType terrain) { this.terrain = terrain; }

    /** @param humidity nouvelle humidité, bornée entre 0.0 et 1.0 */
    public void setHumidity(double humidity) {
        this.humidity = Math.max(0.0, Math.min(1.0, humidity));
    }

    /** @param temperature nouvelle température en °C */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}