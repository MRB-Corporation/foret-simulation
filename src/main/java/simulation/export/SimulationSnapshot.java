package simulation.export;

import simulation.modele.Cell;
import simulation.modele.CellState;
import simulation.modele.Grid;
import simulation.modele.TerrainType;
import simulation.modele.Wind;

/**
 * Immutable snapshot of the simulation state at a single time step.
 *
 * <p>Captures aggregate statistics (cell counts, average physical values) for
 * later export to CSV via {@link SimulationCsvExporter}.</p>
 */
public class SimulationSnapshot {

    private final int step;
    private final int width;
    private final int height;
    private final int totalCells;
    private final int intactCells;
    private final int burningCells;
    private final int burnedCells;
    private final int forestCells;
    private final int wetZoneCells;
    private final int firebreakCells;
    private final int waterCells;
    private final int urbanCells;
    private final double avgTemperature;
    private final double avgHumidity;
    private final double avgInflammability;
    private final double avgResistance;
    private final double avgVegetationDensity;
    private final String algorithmName;
    private final double windDirection;
    private final double windSpeed;

    private SimulationSnapshot(
            int step, int width, int height,
            int totalCells, int intactCells, int burningCells, int burnedCells,
            int forestCells, int wetZoneCells, int firebreakCells,
            int waterCells, int urbanCells,
            double avgTemperature, double avgHumidity, double avgInflammability,
            double avgResistance, double avgVegetationDensity,
            String algorithmName, double windDirection, double windSpeed) {
        this.step = step; this.width = width; this.height = height;
        this.totalCells = totalCells; this.intactCells = intactCells;
        this.burningCells = burningCells; this.burnedCells = burnedCells;
        this.forestCells = forestCells; this.wetZoneCells = wetZoneCells;
        this.firebreakCells = firebreakCells; this.waterCells = waterCells;
        this.urbanCells = urbanCells;
        this.avgTemperature = avgTemperature; this.avgHumidity = avgHumidity;
        this.avgInflammability = avgInflammability; this.avgResistance = avgResistance;
        this.avgVegetationDensity = avgVegetationDensity;
        this.algorithmName = algorithmName;
        this.windDirection = windDirection; this.windSpeed = windSpeed;
    }

    /**
     * Builds a snapshot from the current simulation state.
     *
     * @param grid          current grid
     * @param step          current step number
     * @param algorithmName name of the active spread algorithm
     * @param wind          current wind
     * @return new snapshot
     */
    public static SimulationSnapshot from(Grid grid, int step, String algorithmName, Wind wind) {
        int intact = 0, burning = 0, burned = 0;
        int forest = 0, wet = 0, firebreak = 0, water = 0, urban = 0;
        double temp = 0, hum = 0, inflam = 0, res = 0, density = 0;

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

                temp    += c.getTemperature();
                hum     += c.getHumidity();
                inflam  += c.getInflammability();
                res     += c.getResistance();
                density += c.getVegetationDensity();
            }
        }

        int total = Math.max(1, grid.getWidth() * grid.getHeight());
        return new SimulationSnapshot(
                step, grid.getWidth(), grid.getHeight(),
                total, intact, burning, burned,
                forest, wet, firebreak, water, urban,
                temp / total, hum / total, inflam / total, res / total, density / total,
                algorithmName, wind.getDirection(), wind.getSpeed());
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getStep()                   { return step; }
    public int getWidth()                  { return width; }
    public int getHeight()                 { return height; }
    public int getTotalCells()             { return totalCells; }
    public int getIntactCells()            { return intactCells; }
    public int getBurningCells()           { return burningCells; }
    public int getBurnedCells()            { return burnedCells; }
    public int getForestCells()            { return forestCells; }
    public int getWetZoneCells()           { return wetZoneCells; }
    public int getFirebreakCells()         { return firebreakCells; }
    public int getWaterCells()             { return waterCells; }
    public int getUrbanCells()             { return urbanCells; }
    public double getAvgTemperature()      { return avgTemperature; }
    public double getAvgHumidity()         { return avgHumidity; }
    public double getAvgInflammability()   { return avgInflammability; }
    public double getAvgResistance()       { return avgResistance; }
    public double getAvgVegetationDensity(){ return avgVegetationDensity; }
    public String getAlgorithmName()       { return algorithmName; }
    public double getWindDirection()       { return windDirection; }
    public double getWindSpeed()           { return windSpeed; }
}
