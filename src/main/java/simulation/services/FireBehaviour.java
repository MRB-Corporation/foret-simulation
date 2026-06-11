package simulation.services;

/**
 * Contract that every burnable object in the simulation must fulfil.
 *
 * <p>Separates fire-related behaviour from data storage so that the propagation
 * algorithm can work against this interface rather than a concrete class.</p>
 */
public interface FireBehaviour {

    /**
     * Returns {@code true} if this cell can currently catch fire.
     * A cell can ignite only when it is {@link CellState#INTACT} and its terrain
     * does not block fire.
     *
     * @return {@code true} if ignition is possible
     */
    boolean canIgnite();

    /**
     * Returns {@code true} if this cell type stops fire from propagating through it
     * (water, firebreak, urban zone, wet zone).
     *
     * @return {@code true} if the cell acts as a fire barrier
     */
    boolean blocksfire();

    /**
     * Raises the cell temperature by {@code delta} degrees.
     *
     * @param delta temperature increase in °C (should be ≥ 0)
     */
    void heat(double delta);

    /**
     * Adjusts the cell humidity by {@code delta} (positive = wetter, negative = drier).
     * The result is clamped to [0.0, 1.0].
     *
     * @param delta humidity change
     */
    void adjustHumidity(double delta);
}
