package config;

/**
 * Contient tous les paramètres de la simulation.
 * C'est le seul fichier à modifier pour changer le comportement global.
 */
public class SimulationConfig {

    // ── Dimensions de la grille ────────────────────────────────────────────
    /** Nombre de colonnes dans la grille. */
    public int gridWidth = 60;

    /** Nombre de lignes dans la grille. */
    public int gridHeight = 50;

    /**
     * Si true, les bords de la grille se rejoignent (topologie torique).
     * Le feu qui sort à droite revient à gauche, etc.
     */
    public boolean toricTopology = false;

    // ── Plages de génération aléatoire ─────────────────────────────────────
    /** Humidité minimale d'une cellule à la création (entre 0.0 et 1.0). */
    public double humidityMin = 0.2;

    /** Humidité maximale d'une cellule à la création (entre 0.0 et 1.0). */
    public double humidityMax = 0.8;

    /** Température minimale d'une cellule à la création (en °C). */
    public double temperatureMin = 15.0;

    /** Température maximale d'une cellule à la création (en °C). */
    public double temperatureMax = 35.0;

    // ── Paramètres de propagation ──────────────────────────────────────────
    /**
     * Augmentation de température transmise aux voisins d'une cellule
     * en feu à chaque pas (en °C).
     */
    public double heatTransfer = 10.0;

    /**
     * Baisse d'humidité appliquée aux voisins d'une cellule en feu
     * à chaque pas (entre 0.0 et 1.0).
     */
    public double humidityDrop = 0.05;

    /**
     * Rayon de propagation autour d'une cellule en feu.
     * 1 = voisins directs seulement.
     */
    public int neighborRadius = 1;

    /**
     * Si true, utilise 8 voisins (diagonales incluses).
     * Si false, utilise 4 voisins (orthogonal seulement).
     */
    public boolean useDiagonals = true;

    // ── Combustion ─────────────────────────────────────────────────────────
    /** Nombre de pas pendant lesquels une cellule reste en feu. */
    public int baseBurnDuration = 10;

    /**
     * Température de référence pour l'inflammation (en °C).
     * Plus une cellule s'approche de cette température,
     * plus elle a de chances de s'enflammer.
     */
    public double ignitionTemperature = 100.0;
}