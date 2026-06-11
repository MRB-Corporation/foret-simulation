package simulation.dao;

import simulation.entities.SimulationSnapshot;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Exports a list of {@link SimulationSnapshot} objects to a CSV file.
 *
 * <p>The semicolon (;) is used as delimiter so the file opens correctly in Excel
 * with European locale settings.</p>
 */
public final class SimulationCsvExporter {

    private static final String SEPARATOR = ";";

    private SimulationCsvExporter() {}

    /**
     * Writes all snapshots to the given file path, with a header row.
     *
     * @param history list of snapshots to export
     * @param path    output file path (will be created or overwritten)
     * @throws IOException if the file cannot be written
     */
    public static void export(List<SimulationSnapshot> history, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // Header
            writer.write(String.join(SEPARATOR,
                "step", "width", "height", "total_cells",
                "intact_cells", "burning_cells", "burned_cells",
                "forest_cells", "wet_zone_cells", "firebreak_cells",
                "water_cells", "urban_cells",
                "avg_temperature", "avg_humidity", "avg_inflammability",
                "avg_resistance", "avg_vegetation_density",
                "algorithm", "wind_direction_rad", "wind_speed"
            ));
            writer.newLine();

            for (SimulationSnapshot s : history) {
                writer.write(row(s));
                writer.newLine();
            }
        }
    }

    private static String row(SimulationSnapshot s) {
        return String.join(SEPARATOR,
            Integer.toString(s.getStep()),
            Integer.toString(s.getWidth()),
            Integer.toString(s.getHeight()),
            Integer.toString(s.getTotalCells()),
            Integer.toString(s.getIntactCells()),
            Integer.toString(s.getBurningCells()),
            Integer.toString(s.getBurnedCells()),
            Integer.toString(s.getForestCells()),
            Integer.toString(s.getWetZoneCells()),
            Integer.toString(s.getFirebreakCells()),
            Integer.toString(s.getWaterCells()),
            Integer.toString(s.getUrbanCells()),
            dec(s.getAvgTemperature()),
            dec(s.getAvgHumidity()),
            dec(s.getAvgInflammability()),
            dec(s.getAvgResistance()),
            dec(s.getAvgVegetationDensity()),
            "\"" + s.getAlgorithmName().replace("\"", "\"\"") + "\"",
            dec(s.getWindDirection()),
            dec(s.getWindSpeed())
        );
    }

    private static String dec(double v) {
        return String.format(Locale.US, "%.6f", v);
    }
}
