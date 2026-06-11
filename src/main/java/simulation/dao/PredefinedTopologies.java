package simulation.dao;

import simulation.entities.Cell;
import simulation.entities.Grid;
import simulation.entities.TerrainType;

import java.util.Random;

/**
 * Factory class that provides hand-crafted grid topologies for quick scenario testing.
 *
 * <p>Each method returns a fully initialised {@link Grid} ready to be loaded into
 * the simulator. Seeds are fixed so topologies are reproducible.</p>
 */
public final class PredefinedTopologies {

    private PredefinedTopologies() {}

    // ── 1. Forêt Amazonienne (Brésil) ─────────────────────────────────────────

    /**
     * Très dense, très humide, traversée par un fleuve massif.
     * Le feu a beaucoup de mal à s'y propager à cause de l'humidité.
     */
    public static Grid amazonie() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(42);

        // Base
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.8 + rng.nextDouble() * 0.2); // 80-100%
                c.setHumidity(0.7 + rng.nextDouble() * 0.3);          // 70-100%
                c.setInflammability(0.1 + rng.nextDouble() * 0.2);    // Très faible
                c.setResistance(0.4 + rng.nextDouble() * 0.4);
                applyNoise(c, 0.02, 0.0, 0.05, 0.0, rng); // Un peu d'eau et de zones humides aléatoires
            }
        }

        // Fleuve Amazone (large et sinueux)
        for (int x = 0; x < w; x++) {
            int cy = (int) (h / 2 + Math.sin(x / (double) w * Math.PI * 2) * (h / 4));
            for (int dy = -4; dy <= 4; dy++) {
                Cell c = g.getCell(x, cy + dy);
                if (c != null) {
                    c.setTerrain(TerrainType.WATER);
                    c.setHumidity(1.0);
                    c.setInflammability(0.0);
                }
            }
            // Rives très humides
            for (int dy : new int[]{-6, -5, 5, 6}) {
                Cell c = g.getCell(x, cy + dy);
                if (c != null) {
                    c.setTerrain(TerrainType.WET_ZONE);
                    c.setHumidity(0.9);
                }
            }
        }
        return g;
    }

    // ── 2. Forêt des Landes (France) ──────────────────────────────────────────

    /**
     * Forêt de pins très géométrique, très dense, très inflammable et sèche.
     * Le feu s'y propage extrêmement vite.
     */
    public static Grid foretDesLandes() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(10);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.7 + rng.nextDouble() * 0.2);
                c.setHumidity(0.1 + rng.nextDouble() * 0.2);       // Très sec (10-30%)
                c.setInflammability(0.7 + rng.nextDouble() * 0.3); // Extrêmement inflammable (pins)
                c.setResistance(0.1 + rng.nextDouble() * 0.2);     // Brûle vite
                applyNoise(c, 0.0, 0.02, 0.0, 0.02, rng); // Quelques maisons et chemins de terre épars
            }
        }

        // Pare-feux (chemins forestiers droits typiques des Landes)
        int spacing = w / 5;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (x % spacing == 0 || y % spacing == 0) {
                    Cell c = g.getCell(x, y);
                    c.setTerrain(TerrainType.FIREBREAK);
                    c.setInflammability(0.0);
                }
            }
        }
        return g;
    }

    // ── 3. Brousse Australienne (Outback) ─────────────────────────────────────

    /**
     * Très clairsemée (faible densité), mais atrocement sèche et inflammable.
     */
    public static Grid brousseAustralienne() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(99);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.2 + rng.nextDouble() * 0.3); // Faible densité
                c.setHumidity(0.0 + rng.nextDouble() * 0.1);          // Ultra sec (0-10%)
                c.setInflammability(0.8 + rng.nextDouble() * 0.2);    // 80-100% inflammable
                c.setResistance(0.1 + rng.nextDouble() * 0.2);
                applyNoise(c, 0.0, 0.15, 0.0, 0.01, rng); // Beaucoup de zones de terre battue/sable (pare-feu)
            }
        }
        return g;
    }

    // ── 4. Forêt Boréale (Canada) ─────────────────────────────────────────────

    /**
     * Forêt froide parsemée de milliers de petits lacs.
     */
    public static Grid foretBoreale() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(77);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.5 + rng.nextDouble() * 0.4);
                c.setHumidity(0.4 + rng.nextDouble() * 0.3);
                c.setInflammability(0.3 + rng.nextDouble() * 0.3);
                c.setResistance(0.4 + rng.nextDouble() * 0.3);
                applyNoise(c, 0.03, 0.02, 0.05, 0.01, rng); // Mélange naturel
            }
        }

        // Milliers de petits lacs aléatoires
        int numLakes = (w * h) / 1000;
        for (int i = 0; i < numLakes; i++) {
            int cx = rng.nextInt(w);
            int cy = rng.nextInt(h);
            int radius = 1 + rng.nextInt(3);
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx * dx + dy * dy <= radius * radius) {
                        Cell c = g.getCell(cx + dx, cy + dy);
                        if (c != null) {
                            c.setTerrain(TerrainType.WATER);
                            c.setHumidity(1.0);
                            c.setInflammability(0.0);
                        }
                    }
                }
            }
        }
        return g;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void applyNoise(Cell c, double probWater, double probFirebreak, double probWet, double probUrban, Random rng) {
        double roll = rng.nextDouble();
        if (roll < probWater) {
            c.setTerrain(TerrainType.WATER);
            c.setHumidity(1.0); c.setInflammability(0.0);
        } else if (roll < probWater + probFirebreak) {
            c.setTerrain(TerrainType.FIREBREAK);
            c.setInflammability(0.0);
        } else if (roll < probWater + probFirebreak + probWet) {
            c.setTerrain(TerrainType.WET_ZONE);
            c.setHumidity(0.8); c.setInflammability(0.1);
        } else if (roll < probWater + probFirebreak + probWet + probUrban) {
            c.setTerrain(TerrainType.URBAN_ZONE);
            c.setInflammability(0.0);
        }
    }
}
