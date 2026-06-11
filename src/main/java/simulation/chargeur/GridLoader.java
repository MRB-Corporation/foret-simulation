package simulation.chargeur;

import simulation.modele.Cell;
import simulation.modele.CellState;
import simulation.modele.Grid;
import simulation.modele.TerrainType;
import simulation.utilitaire.Constants;

import java.io.*;
import java.util.Random;

/**
 * Utility class for generating, saving and loading {@link Grid} objects.
 *
 * <p>CSV format (one header line, then one line per cell):</p>
 * <pre>width,height
 * x,y,state,terrain,humidity,inflammability,resistance,density,temperature</pre>
 */
public class GridLoader {

    private static final Random RANDOM = new Random();

    private GridLoader() {}

    // ── Random generation ─────────────────────────────────────────────────────

    /**
     * Generates a random grid of the given size.
     *
     * @param width  number of columns
     * @param height number of rows
     * @return new randomly populated grid
     */
    public static Grid generateRandom(int width, int height) {
        Grid grid = new Grid(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                applyRandomState(grid.getCell(x, y));
            }
        }
        return grid;
    }

    /** Generates a random grid with default dimensions. */
    public static Grid generateRandom() {
        return generateRandom(Grid.DEFAULT_WIDTH, Grid.DEFAULT_HEIGHT);
    }

    /**
     * Assigns a random terrain type and matching physical attributes to a cell.
     *
     * @param cell the cell to configure
     */
    private static void applyRandomState(Cell cell) {
        double roll = RANDOM.nextDouble();

        if (roll < Constants.PROB_WATER) {
            cell.setTerrain(TerrainType.WATER);
            cell.setHumidity(1.0);
            cell.setInflammability(0.0);

        } else if (roll < Constants.PROB_FIREBREAK) {
            cell.setTerrain(TerrainType.FIREBREAK);
            cell.setInflammability(0.0);

        } else if (roll < Constants.PROB_WET_ZONE) {
            cell.setTerrain(TerrainType.WET_ZONE);
            cell.setHumidity(Constants.WET_ZONE_HUMIDITY);
            cell.setInflammability(Constants.WET_ZONE_INFLAM);

        } else if (roll < Constants.PROB_URBAN) {
            cell.setTerrain(TerrainType.URBAN_ZONE);
            cell.setInflammability(0.0);

        } else {
            cell.setTerrain(TerrainType.FOREST);
            cell.setHumidity(Constants.FOREST_HUMIDITY_MIN
                    + RANDOM.nextDouble() * Constants.FOREST_HUMIDITY_RANGE);
            cell.setInflammability(Constants.FOREST_INFLAM_MIN
                    + RANDOM.nextDouble() * Constants.FOREST_INFLAM_RANGE);
            cell.setResistance(Constants.FOREST_RESISTANCE_MIN
                    + RANDOM.nextDouble() * Constants.FOREST_RESISTANCE_RANGE);
            cell.setVegetationDensity(Constants.FOREST_DENSITY_MIN
                    + RANDOM.nextDouble() * Constants.FOREST_DENSITY_RANGE);
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    /**
     * Saves the grid to a CSV file.
     *
     * @param grid  grid to save
     * @param path  output file path
     */
    public static void save(Grid grid, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(grid.getWidth() + "," + grid.getHeight());
            writer.newLine();

            for (int y = 0; y < grid.getHeight(); y++) {
                for (int x = 0; x < grid.getWidth(); x++) {
                    Cell c = grid.getCell(x, y);
                    writer.write(
                        x + "," + y + "," +
                        c.getState().name() + "," +
                        c.getTerrain().name() + "," +
                        c.getHumidity() + "," +
                        c.getInflammability() + "," +
                        c.getResistance() + "," +
                        c.getVegetationDensity() + "," +
                        c.getTemperature()
                    );
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads a grid from a CSV file previously saved by {@link #save}.
     * Returns an empty default grid if the file cannot be read.
     *
     * @param path CSV file path
     * @return loaded grid
     */
    public static Grid load(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String[] dimensions = reader.readLine().split(",");
            int width  = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);

            Grid grid = new Grid(width, height);
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);

                Cell c = grid.getCell(x, y);
                c.setState(CellState.valueOf(parts[2]));
                c.setTerrain(TerrainType.valueOf(parts[3]));
                c.setHumidity(Double.parseDouble(parts[4]));
                c.setInflammability(Double.parseDouble(parts[5]));
                c.setResistance(Double.parseDouble(parts[6]));
                c.setVegetationDensity(Double.parseDouble(parts[7]));
                c.setTemperature(Double.parseDouble(parts[8]));
            }
            return grid;

        } catch (IOException e) {
            System.err.println("Load error: " + e.getMessage());
            return new Grid();
        }
    }
}
