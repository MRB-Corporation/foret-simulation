package simulation.services;

import simulation.entities.Cell;
import simulation.entities.Grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Fire-spread algorithm based on the Moore neighbourhood: the 8 surrounding cells
 * (4 orthogonal + 4 diagonal).
 *
 * <p>Diagonal neighbours are naturally weighted by the wind formula because the
 * distance ratio (1/√2) is implicitly captured by {@code atan2} in
 * {@link simulation.utilitaire.PhysicsFormulas#windFactor}.</p>
 */
public class MooreSpread implements SpreadAlgorithm {

    /** The 8 Moore directions as (dx, dy) offsets. */
    private static final int[][] DIRECTIONS = {
        { 0, -1},   // North
        { 0,  1},   // South
        { 1,  0},   // East
        {-1,  0},   // West
        { 1, -1},   // North-East
        {-1, -1},   // North-West
        { 1,  1},   // South-East
        {-1,  1}    // South-West
    };

    /**
     * {@inheritDoc}
     *
     * <p>Returns the (up to) 8 Moore neighbours that exist within the grid.</p>
     */
    @Override
    public List<Cell> getNeighbours(Cell cell, Grid grid) {
        List<Cell> neighbours = new ArrayList<>();
        int x = cell.getX();
        int y = cell.getY();

        for (int[] dir : DIRECTIONS) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (grid.isValid(nx, ny)) {
                neighbours.add(grid.getCell(nx, ny));
            }
        }
        return neighbours;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Moore Spread";
    }
}
