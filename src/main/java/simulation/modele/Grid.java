package simulation.modele;

import simulation.utilitaire.Constants;

import java.util.Random;

/**
 * Two-dimensional grid of {@link Cell} objects that represents the simulation terrain.
 *
 * <p>The grid supports two topology modes:</p>
 * <ul>
 *   <li><b>Bounded</b> — cells at the border have no neighbours beyond the edge.</li>
 *   <li><b>Toroidal</b> — the left edge wraps to the right edge, and the top edge
 *       wraps to the bottom edge, creating a doughnut-shaped surface.</li>
 * </ul>
 *
 * <p>The grid can also grow dynamically (bounded mode only, up to
 * {@link #MAX_WIDTH} × {@link #MAX_HEIGHT}) when the camera scrolls near an edge.</p>
 */
public class Grid {

    // ── Limits & defaults ─────────────────────────────────────────────────────

    /** Maximum allowed grid width in cells. */
    public static final int MAX_WIDTH = 500;

    /** Maximum allowed grid height in cells. */
    public static final int MAX_HEIGHT = 500;

    /** Default grid width used when no size is specified. */
    public static final int DEFAULT_WIDTH = 50;

    /** Default grid height used when no size is specified. */
    public static final int DEFAULT_HEIGHT = 50;

    // ── Fields ────────────────────────────────────────────────────────────────

    private int width;
    private int height;
    private Cell[][] cells;
    private boolean toroidal;
    private static final Random RANDOM = new Random();

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * Creates a bounded grid of the given size filled with randomly typed cells.
     *
     * @param width  number of columns (> 0)
     * @param height number of rows    (> 0)
     */
    public Grid(int width, int height) {
        this(width, height, false);
    }

    /**
     * Creates a grid with the given size, topology, and random cell content.
     *
     * @param width     number of columns
     * @param height    number of rows
     * @param toroidal  {@code true} for toroidal (wrap-around) topology
     */
    public Grid(int width, int height, boolean toroidal) {
        this.width     = width;
        this.height    = height;
        this.toroidal  = toroidal;
        this.cells     = new Cell[height][width];
        initialise();
    }

    /** Creates a bounded grid with {@link #DEFAULT_WIDTH} × {@link #DEFAULT_HEIGHT} cells. */
    public Grid() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /** Fills every position with a randomly generated cell. */
    private void initialise() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                cells[y][x] = createRandomCell(x, y);
    }

    /**
     * Creates a single cell at (x, y) with a random terrain type.
     *
     * @param x column
     * @param y row
     * @return new cell
     */
    private Cell createRandomCell(int x, int y) {
        Cell cell = new Cell(x, y);
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
        return cell;
    }

    // ── Cell access ───────────────────────────────────────────────────────────

    /**
     * Returns the cell at (x, y).
     *
     * <p>In <b>toroidal</b> mode coordinates wrap around automatically.
     * In <b>bounded</b> mode returns {@code null} if out of bounds.</p>
     *
     * @param x column
     * @param y row
     * @return cell, or {@code null} if out of bounds in bounded mode
     */
    public Cell getCell(int x, int y) {
        if (toroidal) {
            x = Math.floorMod(x, width);
            y = Math.floorMod(y, height);
            return cells[y][x];
        }
        return isValid(x, y) ? cells[y][x] : null;
    }

    /**
     * Replaces the cell at (x, y). Does nothing if out of bounds.
     *
     * @param x column
     * @param y row
     * @param c new cell
     */
    public void setCell(int x, int y, Cell c) {
        if (toroidal) {
            x = Math.floorMod(x, width);
            y = Math.floorMod(y, height);
            cells[y][x] = c;
        } else if (isValid(x, y)) {
            cells[y][x] = c;
        }
    }

    /**
     * Returns {@code true} if (x, y) is within the current grid bounds.
     * Always {@code true} in toroidal mode after wrapping.
     *
     * @param x column
     * @param y row
     * @return validity flag
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // ── Dynamic growth (bounded mode only) ───────────────────────────────────

    /**
     * Expands the grid by adding new rows/columns on the requested sides.
     * Has no effect in toroidal mode (the grid wraps and never needs to grow).
     * Growth is capped so the grid never exceeds {@link #MAX_WIDTH} × {@link #MAX_HEIGHT}.
     *
     * @param left   columns to add on the left
     * @param top    rows to add at the top
     * @param right  columns to add on the right
     * @param bottom rows to add at the bottom
     */
    public void expand(int left, int top, int right, int bottom) {
        if (toroidal) return; // toroidal grids never need expansion

        left   = Math.max(0, left);
        top    = Math.max(0, top);
        right  = Math.max(0, right);
        bottom = Math.max(0, bottom);

        int addableW = Math.max(0, MAX_WIDTH  - width);
        int addableH = Math.max(0, MAX_HEIGHT - height);

        if (left + right > addableW) {
            int reqLeft = left;
            left  = Math.min(left,  addableW);
            right = Math.min(right, addableW - left);
            int rem = addableW - left - right;
            if (rem > 0 && reqLeft > left) left += Math.min(rem, reqLeft - left);
        }
        if (top + bottom > addableH) {
            int reqTop = top;
            top    = Math.min(top,    addableH);
            bottom = Math.min(bottom, addableH - top);
            int rem = addableH - top - bottom;
            if (rem > 0 && reqTop > top) top += Math.min(rem, reqTop - top);
        }
        if (left == 0 && top == 0 && right == 0 && bottom == 0) return;

        int newW = width + left + right;
        int newH = height + top + bottom;
        Cell[][] newCells = new Cell[newH][newW];

        for (int y = 0; y < newH; y++) {
            for (int x = 0; x < newW; x++) {
                int ox = x - left, oy = y - top;
                newCells[y][x] = isValid(ox, oy)
                        ? copyCell(cells[oy][ox], x, y)
                        : createRandomCell(x, y);
            }
        }
        width  = newW;
        height = newH;
        cells  = newCells;
    }

    private Cell copyCell(Cell source, int x, int y) {
        Cell copy = new Cell(x, y);
        copy.setState(source.getState());
        copy.setTerrain(source.getTerrain());
        copy.setHumidity(source.getHumidity());
        copy.setInflammability(source.getInflammability());
        copy.setResistance(source.getResistance());
        copy.setVegetationDensity(source.getVegetationDensity());
        copy.setTemperature(source.getTemperature());
        copy.setBurnCounter(source.getBurnCounter());
        return copy;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /** @return number of columns */
    public int getWidth()  { return width; }

    /** @return number of rows */
    public int getHeight() { return height; }

    /**
     * Returns {@code true} if this grid uses toroidal (wrap-around) topology.
     * @return toroidal flag
     */
    public boolean isToroidal() { return toroidal; }

    /**
     * Switches topology mode. Changing to toroidal on an already-populated grid
     * preserves all existing cell data.
     *
     * @param toroidal {@code true} for toroidal topology
     */
    public void setToroidal(boolean toroidal) { this.toroidal = toroidal; }
}
