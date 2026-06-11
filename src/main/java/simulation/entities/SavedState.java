package simulation.entities;

/**
 * Holds the result of a binary load operation.
 */
public class SavedState {
    /** Restored grid. */
    public final Grid grid;
    /** Restored wind. */
    public final Wind wind;

    public SavedState(Grid grid, Wind wind) {
        this.grid = grid;
        this.wind = wind;
    }
}
