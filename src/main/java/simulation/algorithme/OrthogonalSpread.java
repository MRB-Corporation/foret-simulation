package simulation.algorithme;

import simulation.modele.Cell;
import simulation.modele.Grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Fire-spread algorithm that considers only the 4 orthogonal neighbours
 * (North, South, East, West).
 *
 * <p>This is the simplest neighbourhood. Fire spreads in a diamond/rhombus shape
 * and cannot jump diagonally.</p>
 */
public class OrthogonalSpread implements SpreadAlgorithm {

    /** The 4 cardinal directions as (dx, dy) offsets. */
    private static final int[][] DIRECTIONS = {
        { 0, -1},   // North
        { 0,  1},   // South
        { 1,  0},   // East
        {-1,  0}    // West
    };

    /**
     * {@inheritDoc}
     *
     * <p>Returns the (up to) 4 orthogonal neighbours that exist within the grid,
     * regardless of their state or terrain type.</p>
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
        return "Orthogonal Spread";
    }
}
