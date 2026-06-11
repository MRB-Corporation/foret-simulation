package simulation.services;

import simulation.entities.Cell;
import simulation.entities.Grid;

import java.util.List;

/**
 * Strategy interface for fire-spread algorithms.
 *
 * <p>The two responsibilities are intentionally separated:</p>
 * <ul>
 *   <li>{@link #getNeighbours(Cell, Grid)} — purely geometric: which cells are
 *       reachable from the given cell according to this algorithm's neighbourhood
 *       definition. No filtering on terrain or state is done here.</li>
 *   <li>{@link #getCandidates(Cell, Grid)} — returns only the neighbours that
 *       could actually catch fire (i.e. those that pass {@link Cell#canIgnite()}).
 *       This is the list the simulator feeds into the physics formulas.</li>
 * </ul>
 *
 * <p>This separation makes it easy to debug or extend either part independently.</p>
 */
public interface SpreadAlgorithm {

    /**
     * Returns ALL geometrically reachable neighbours of {@code cell} according
     * to this algorithm, regardless of their terrain type or fire state.
     *
     * @param cell  the source cell (must not be null)
     * @param grid  the full simulation grid (must not be null)
     * @return list of neighbouring cells (never null, may be empty)
     */
    List<Cell> getNeighbours(Cell cell, Grid grid);

    /**
     * Returns the subset of {@link #getNeighbours(Cell, Grid)} that can currently
     * catch fire — i.e. cells where {@link Cell#canIgnite()} returns {@code true}.
     *
     * <p>Default implementation filters the result of {@code getNeighbours}.
     * Override only if the algorithm needs custom filtering logic.</p>
     *
     * @param cell  the burning source cell
     * @param grid  the full simulation grid
     * @return ignitable neighbours
     */
    default List<Cell> getCandidates(Cell cell, Grid grid) {
        List<Cell> neighbours = getNeighbours(cell, grid);
        neighbours.removeIf(c -> !c.canIgnite());
        return neighbours;
    }

    /**
     * Human-readable name shown in the GUI combo box.
     *
     * @return algorithm name
     */
    String getName();
}
