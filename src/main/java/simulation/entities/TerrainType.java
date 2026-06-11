package simulation.entities;

/**
 * Types of terrain that a grid cell can represent.
 *
 * <p>Some terrain types block fire spread entirely (water, firebreak, urban zone, wet zone).</p>
 */
public enum TerrainType {

    /** Standard forest — can catch fire. */
    FOREST,

    /** Humid area — high moisture, very low inflammability. */
    WET_ZONE,

    /** Cleared strip that stops fire from spreading. */
    FIREBREAK,

    /** Water body — completely non-flammable. */
    WATER,

    /** Built-up urban area — treated as non-flammable in this simulation. */
    URBAN_ZONE
}
