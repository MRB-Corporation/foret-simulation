package model;

/**
 * Représente l'état d'une cellule.
 * Une cellule suit toujours le cycle : INTACT → BURNING → BURNED.
 */
public enum CellState {

    /** La cellule est intacte, elle peut potentiellement prendre feu. */
    INTACT,

    /** La cellule est en train de brûler. */
    BURNING,

    /** La cellule a entièrement brûlé, elle ne peut plus prendre feu. */
    BURNED
}