package simulation.algorithme;

import simulation.modele.Cell;
import simulation.modele.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fire-spread algorithm that considers all cells within a circular radius {@code R}.
 *
 * <p>Each candidate cell is included with a probability of 1/distance², simulating
 * embers or sparks that travel farther with decreasing likelihood:</p>
 * <ul>
 *   <li>distance 1  → 100 % included</li>
 *   <li>distance √2 → 50 %</li>
 *   <li>distance 2  → 25 %</li>
 *   <li>distance 3  → 11 %</li>
 * </ul>
 *
 * <p>Note: {@link #getNeighbours} applies the stochastic filter so that the
 * geometry and the probability model stay together. {@link #getCandidates} then
 * additionally filters on {@link Cell#canIgnite()}.</p>
 */
public class RadialSpread implements SpreadAlgorithm {

    /** Default radius in cells. */
    public static final int DEFAULT_RADIUS = 3;

    private int radius;
    private static final Random RNG = new Random();

    /** Creates a radial algorithm with the default radius. */
    public RadialSpread() {
        this(DEFAULT_RADIUS);
    }

    /**
     * Creates a radial algorithm with a custom radius.
     *
     * @param radius maximum spread distance in cells (> 0)
     */
    public RadialSpread(int radius) {
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Iterates over all cells within the bounding square [−radius, +radius]²,
     * retains those within the circular radius, and applies the 1/distance²
     * stochastic inclusion filter.</p>
     */
    @Override
    public List<Cell> getNeighbours(Cell cell, Grid grid) {
        List<Cell> neighbours = new ArrayList<>();
        int x = cell.getX();
        int y = cell.getY();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance <= 0 || distance > radius) continue;

                // Stochastic inclusion: probability = 1 / distance²
                if (RNG.nextDouble() > 1.0 / (distance * distance)) continue;

                int nx = x + dx;
                int ny = y + dy;
                if (grid.isValid(nx, ny)) {
                    neighbours.add(grid.getCell(nx, ny));
                }
            }
        }
        return neighbours;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Radial Spread (radius " + radius + ")";
    }

    /** @return current radius */
    public int getRadius() { return radius; }

    /**
     * Changes the spread radius.
     * @param radius new radius (> 0)
     */
    public void setRadius(int radius) { this.radius = radius; }
}
