package model;

/**
 * Représente le type de terrain d'une cellule.
 * Le terrain influence la possibilité de s'enflammer.
 */
public enum TerrainType {

    /** Forêt dense, s'enflamme facilement. */
    FOREST,

    /** Zone humide, plus difficile à enflammer. */
    WET_ZONE,

    /** Coupe-feu, bloque la propagation. */
    FIREBREAK,

    /** Eau, ne peut jamais brûler. */
    WATER,

    /** Zone urbanisée, ne brûle pas. */
    URBAN
}