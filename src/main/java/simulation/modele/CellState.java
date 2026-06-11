package simulation.modele;

/**
 * Possible fire states of a grid cell.
 *
 * <p>Transitions: {@code INTACT} → {@code ON_FIRE} → {@code BURNED}</p>
 */
public enum CellState {

    /** The cell has not yet been reached by fire. */
    INTACT,

    /** The cell is currently burning. */
    ON_FIRE,

    /** The cell has finished burning and cannot catch fire again. */
    BURNED
}
