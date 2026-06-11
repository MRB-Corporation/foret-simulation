package simulation.entities;

import simulation.entities.Cell;
import simulation.entities.CellState;
import simulation.entities.Grid;
import simulation.entities.TerrainType;

/**
 * Computes live statistics from the current grid state.
 *
 * <p>An instance is created each time step by calling {@link #compute(Grid)}.
 * It is then passed to the {@link StatsPanel} for display.</p>
 */
public class GridStats {

    // ── Cell state counts ─────────────────────────────────────────────────────

    private final int totalCells;
    private final int intactCells;
    private final int burningCells;
    private final int burnedCells;

    // ── Terrain counts ────────────────────────────────────────────────────────

    private final int forestCells;
    private final int wetZoneCells;
    private final int firebreakCells;
    private final int waterCells;
    private final int urbanCells;

    // ── Average physical values ───────────────────────────────────────────────

    private final double avgHumidity;
    private final double avgTemperature;
    private final double avgInflammability;

    // ── Step number ───────────────────────────────────────────────────────────

    private final int step;

    // ── Constructor (private — use factory method) ────────────────────────────

    private GridStats(int step,
                      int totalCells, int intactCells, int burningCells, int burnedCells,
                      int forestCells, int wetZoneCells, int firebreakCells,
                      int waterCells, int urbanCells,
                      double avgHumidity, double avgTemperature, double avgInflammability) {
        this.step              = step;
        this.totalCells        = totalCells;
        this.intactCells       = intactCells;
        this.burningCells      = burningCells;
        this.burnedCells       = burnedCells;
        this.forestCells       = forestCells;
        this.wetZoneCells      = wetZoneCells;
        this.firebreakCells    = firebreakCells;
        this.waterCells        = waterCells;
        this.urbanCells        = urbanCells;
        this.avgHumidity       = avgHumidity;
        this.avgTemperature    = avgTemperature;
        this.avgInflammability = avgInflammability;
    }

    /**
     * Scans the full grid and returns a new {@link GridStats} instance.
     *
     * @param grid current grid (must not be null)
     * @param step current simulation step
     * @return computed statistics
     */
    public static GridStats compute(Grid grid, int step) {
        int intact = 0, burning = 0, burned = 0;
        int forest = 0, wet = 0, firebreak = 0, water = 0, urban = 0;
        double hum = 0, temp = 0, inflam = 0;

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell c = grid.getCell(x, y);

                if      (c.getState() == CellState.INTACT)   intact++;
                else if (c.getState() == CellState.ON_FIRE)  burning++;
                else if (c.getState() == CellState.BURNED)   burned++;

                if      (c.getTerrain() == TerrainType.FOREST)     forest++;
                else if (c.getTerrain() == TerrainType.WET_ZONE)   wet++;
                else if (c.getTerrain() == TerrainType.FIREBREAK)  firebreak++;
                else if (c.getTerrain() == TerrainType.WATER)      water++;
                else if (c.getTerrain() == TerrainType.URBAN_ZONE) urban++;

                hum    += c.getHumidity();
                temp   += c.getTemperature();
                inflam += c.getInflammability();
            }
        }

        int total = Math.max(1, grid.getWidth() * grid.getHeight());
        return new GridStats(step,
                total, intact, burning, burned,
                forest, wet, firebreak, water, urban,
                hum / total, temp / total, inflam / total);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return current simulation step */
    public int getStep()              { return step; }

    /** @return total number of cells */
    public int getTotalCells()        { return totalCells; }

    /** @return number of intact (unburned) cells */
    public int getIntactCells()       { return intactCells; }

    /** @return number of currently burning cells */
    public int getBurningCells()      { return burningCells; }

    /** @return number of fully burned cells */
    public int getBurnedCells()       { return burnedCells; }

    /** @return percentage of cells that are burning (0–100) */
    public double getBurningPercent() { return 100.0 * burningCells / totalCells; }

    /** @return percentage of cells that are burned (0–100) */
    public double getBurnedPercent()  { return 100.0 * burnedCells  / totalCells; }

    /** @return percentage of cells that are intact (0–100) */
    public double getIntactPercent()  { return 100.0 * intactCells  / totalCells; }

    /** @return number of forest cells */
    public int getForestCells()       { return forestCells; }

    /** @return number of wet zone cells */
    public int getWetZoneCells()      { return wetZoneCells; }

    /** @return number of firebreak cells */
    public int getFirebreakCells()    { return firebreakCells; }

    /** @return number of water cells */
    public int getWaterCells()        { return waterCells; }

    /** @return number of urban zone cells */
    public int getUrbanCells()        { return urbanCells; }

    /** @return average humidity across all cells */
    public double getAvgHumidity()       { return avgHumidity; }

    /** @return average temperature across all cells */
    public double getAvgTemperature()    { return avgTemperature; }

    /** @return average inflammability across all cells */
    public double getAvgInflammability() { return avgInflammability; }
}
