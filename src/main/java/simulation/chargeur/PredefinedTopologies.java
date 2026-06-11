package simulation.chargeur;

import simulation.modele.Cell;
import simulation.modele.Grid;
import simulation.modele.TerrainType;

import java.util.Random;

/**
 * Factory class that provides hand-crafted grid topologies for quick scenario testing.
 *
 * <p>Each method returns a fully initialised {@link Grid} ready to be loaded into
 * the simulator. Seeds are fixed so topologies are reproducible.</p>
 */
public final class PredefinedTopologies {

    private PredefinedTopologies() {}

    // ── 1. Dense forest ───────────────────────────────────────────────────────

    /**
     * Dense forest with low humidity and high inflammability — fire spreads fast.
     * Scattered wet-zone refuges slow the fire locally.
     *
     * @return dense-forest grid
     */
    public static Grid denseForest() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(42);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.75 + rng.nextDouble() * 0.25);
                c.setHumidity(0.05 + rng.nextDouble() * 0.15);
                c.setInflammability(0.65 + rng.nextDouble() * 0.25);
                c.setResistance(0.3 + rng.nextDouble() * 0.3);
            }
        }

        // Circular wet-zone patches acting as natural refuges
        int[][] centres = {{10,8},{15,35},{38,12},{25,42},{5,25},{42,30},{30,20}};
        for (int[] p : centres) {
            int radius = 2 + rng.nextInt(2);
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx * dx + dy * dy <= radius * radius) {
                        Cell c = g.getCell(p[0] + dx, p[1] + dy);
                        if (c != null) {
                            c.setTerrain(TerrainType.WET_ZONE);
                            c.setHumidity(0.70 + rng.nextDouble() * 0.25);
                            c.setInflammability(0.05 + rng.nextDouble() * 0.05);
                        }
                    }
                }
            }
        }
        return g;
    }

    // ── 2. River through forest ───────────────────────────────────────────────

    /**
     * A diagonal river cuts through the forest with humid banks on each side.
     * Fire spreads freely on both banks but cannot cross the water.
     *
     * @return river-in-forest grid
     */
    public static Grid riverInForest() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);
        Random rng = new Random(7);

        // Base forest
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.4 + rng.nextDouble() * 0.5);
                c.setHumidity(0.1 + rng.nextDouble() * 0.25);
                c.setInflammability(0.45 + rng.nextDouble() * 0.35);
                c.setResistance(0.2 + rng.nextDouble() * 0.3);
            }
        }

        // Diagonal river: row 15 at x=0 → row 35 at x=49
        for (int x = 0; x < w; x++) {
            int cy = 15 + x * 20 / (w - 1);

            // River core (3 cells wide)
            for (int dy = -1; dy <= 1; dy++) {
                Cell c = g.getCell(x, cy + dy);
                if (c != null) {
                    c.setTerrain(TerrainType.WATER);
                    c.setHumidity(1.0);
                    c.setInflammability(0.0);
                }
            }
            // Humid banks (2 cells each side)
            for (int dy : new int[]{-3, -2, 2, 3}) {
                Cell c = g.getCell(x, cy + dy);
                if (c != null && c.getTerrain() == TerrainType.FOREST) {
                    c.setTerrain(TerrainType.WET_ZONE);
                    c.setHumidity(0.70 + rng.nextDouble() * 0.2);
                    c.setInflammability(0.05 + rng.nextDouble() * 0.05);
                }
            }
        }
        return g;
    }

    // ── 3. City with courtyard ────────────────────────────────────────────────

    /**
     * Urban blocks separated by firebreak streets surrounding a central park.
     * The park contains a water fountain at its centre.
     *
     * <p>Grid layout (50×50, streets at columns/rows 5, 16, 33, 44):</p>
     * <pre>
     * [edge][street][block][street][   park   ][street][block][street][edge]
     *  0-4    5     6-15    16    17-32         33    34-43    44    45-49
     * </pre>
     *
     * @return city-with-courtyard grid
     */
    public static Grid cityWithCourtyard() {
        int w = Grid.DEFAULT_WIDTH, h = Grid.DEFAULT_HEIGHT;
        Grid g = new Grid(w, h);

        // Default: all urban (non-flammable)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.URBAN_ZONE);
                c.setInflammability(0.0);
            }
        }

        // Firebreak streets forming the urban grid
        int[] streets = {5, 16, 33, 44};
        for (int s : streets) {
            for (int i = 0; i < w; i++) {
                applyStreet(g, i, s);   // horizontal
                applyStreet(g, s, i);   // vertical
            }
        }

        // Central park: x=[17,32], y=[17,32]
        Random rng = new Random(13);
        for (int y = 17; y <= 32; y++) {
            for (int x = 17; x <= 32; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.FOREST);
                c.setVegetationDensity(0.60 + rng.nextDouble() * 0.30);
                c.setHumidity(0.30 + rng.nextDouble() * 0.15);
                c.setInflammability(0.40 + rng.nextDouble() * 0.20);
                c.setResistance(0.25 + rng.nextDouble() * 0.20);
            }
        }

        // Fountain at park centre: x=[22,27], y=[22,27]
        for (int y = 22; y <= 27; y++) {
            for (int x = 22; x <= 27; x++) {
                Cell c = g.getCell(x, y);
                c.setTerrain(TerrainType.WATER);
                c.setHumidity(1.0);
                c.setInflammability(0.0);
            }
        }

        return g;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void applyStreet(Grid g, int x, int y) {
        Cell c = g.getCell(x, y);
        if (c != null) {
            c.setTerrain(TerrainType.FIREBREAK);
            c.setInflammability(0.0);
        }
    }
}
