package simulation.serialisation;

import simulation.modele.Cell;
import simulation.modele.CellState;
import simulation.modele.Grid;
import simulation.modele.TerrainType;
import simulation.modele.Wind;

import java.io.*;

/**
 * Saves and restores the complete simulation state using Java binary serialisation.
 *
 * <p>The binary format stores every cell attribute exactly, so a loaded grid
 * is bit-for-bit identical to the saved one — unlike the CSV format which
 * may lose floating-point precision.</p>
 *
 * <p>File layout (DataOutputStream):</p>
 * <pre>
 * [int]     width
 * [int]     height
 * [boolean] toroidal
 * [double]  wind direction
 * [double]  wind speed
 * for each cell (row-major order):
 *   [int]    CellState  ordinal
 *   [int]    TerrainType ordinal
 *   [double] humidity
 *   [double] inflammability
 *   [double] resistance
 *   [double] vegetationDensity
 *   [double] temperature
 *   [int]    burnCounter
 * </pre>
 */
public final class BinarySerializer {

    /** File extension used for binary save files. */
    public static final String EXTENSION = ".ffsave";

    private BinarySerializer() {}

    // ── Save ──────────────────────────────────────────────────────────────────

    /**
     * Saves the grid and wind to a binary file.
     *
     * @param grid grid to save (must not be null)
     * @param wind current wind (must not be null)
     * @param path output file path
     * @throws IOException if the file cannot be written
     */
    public static void save(Grid grid, Wind wind, String path) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {

            out.writeInt(grid.getWidth());
            out.writeInt(grid.getHeight());
            out.writeBoolean(grid.isToroidal());
            out.writeDouble(wind.getDirection());
            out.writeDouble(wind.getSpeed());

            for (int y = 0; y < grid.getHeight(); y++) {
                for (int x = 0; x < grid.getWidth(); x++) {
                    Cell c = grid.getCell(x, y);
                    out.writeInt(c.getState().ordinal());
                    out.writeInt(c.getTerrain().ordinal());
                    out.writeDouble(c.getHumidity());
                    out.writeDouble(c.getInflammability());
                    out.writeDouble(c.getResistance());
                    out.writeDouble(c.getVegetationDensity());
                    out.writeDouble(c.getTemperature());
                    out.writeInt(c.getBurnCounter());
                }
            }
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Holds the result of a binary load operation.
     */
    public static class SavedState {
        /** Restored grid. */
        public final Grid grid;
        /** Restored wind. */
        public final Wind wind;

        SavedState(Grid grid, Wind wind) {
            this.grid = grid;
            this.wind = wind;
        }
    }

    /**
     * Loads a simulation state from a binary file previously created by {@link #save}.
     *
     * @param path file path to read
     * @return the restored grid and wind
     * @throws IOException if the file cannot be read or is corrupted
     */
    public static SavedState load(String path) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {

            int     width     = in.readInt();
            int     height    = in.readInt();
            boolean toroidal  = in.readBoolean();
            double  windDir   = in.readDouble();
            double  windSpd   = in.readDouble();

            Grid grid = new Grid(width, height, toroidal);
            Wind wind = new Wind(windDir, windSpd);

            CellState[]   states   = CellState.values();
            TerrainType[] terrains = TerrainType.values();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Cell c = grid.getCell(x, y);
                    c.setState(states[in.readInt()]);
                    c.setTerrain(terrains[in.readInt()]);
                    c.setHumidity(in.readDouble());
                    c.setInflammability(in.readDouble());
                    c.setResistance(in.readDouble());
                    c.setVegetationDensity(in.readDouble());
                    c.setTemperature(in.readDouble());
                    c.setBurnCounter(in.readInt());
                }
            }
            return new SavedState(grid, wind);
        }
    }
}
