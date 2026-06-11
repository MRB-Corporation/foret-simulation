package simulation.utilitaire;

import simulation.modele.Cell;
import simulation.modele.Wind;

/**
 * Stateless utility class that groups all physical formulas used by the simulation.
 *
 * <p>Every method is pure (no side effects) so it is easy to test and reason about.</p>
 */
public final class PhysicsFormulas {

    private PhysicsFormulas() {}

    /**
     * Base probability that a cell catches fire when exposed to a burning neighbour.
     *
     * <p>Formula: P = inflammability × (1 − humidity) × (1 − resistance) × vegetationDensity</p>
     *
     * @param c the candidate cell (must not be null)
     * @return probability in [0.0, 1.0]
     */
    public static double ignitionProbability(Cell c) {
        return c.getInflammability()
             * (1.0 - c.getHumidity())
             * (1.0 - c.getResistance())
             * c.getVegetationDensity();
    }

    /**
     * Wind factor that amplifies or reduces fire spread toward a given direction.
     *
     * <p>Formula: factor = 1 + (speed / maxSpeed) × cos(windAngle − targetAngle)</p>
     *
     * @param wind the current wind (must not be null)
     * @param dx   column offset from burning cell to target neighbour
     * @param dy   row offset from burning cell to target neighbour
     * @return multiplicative factor ≥ 0
     */
    public static double windFactor(Wind wind, int dx, int dy) {
        double angleToNeighbour = Math.atan2(dy, dx);
        return 1.0 + (wind.getSpeed() / Wind.MAX_SPEED)
                   * Math.cos(wind.getDirection() - angleToNeighbour);
    }

    /**
     * Ignition probability adjusted for wind direction and speed.
     *
     * @param c    target cell
     * @param wind current wind
     * @param dx   column offset (burning → target)
     * @param dy   row offset   (burning → target)
     * @return probability in [0.0, ∞) — clamped to 1.0 before use
     */
    public static double ignitionProbabilityWithWind(Cell c, Wind wind, int dx, int dy) {
        return ignitionProbability(c) * windFactor(wind, dx, dy);
    }

    /**
     * Temperature increase received by a neighbour from an adjacent burning cell.
     *
     * <p>Formula: Δt = (fireTmp − neighbourTmp) × conductivity × (1 − humidity)</p>
     *
     * @param neighbour the cell being heated
     * @return temperature delta in °C (always ≥ 0)
     */
    public static double temperatureDelta(Cell neighbour) {
        return (Constants.FIRE_TEMPERATURE - neighbour.getTemperature())
             * Constants.HEAT_CONDUCTIVITY
             * (1.0 - neighbour.getHumidity());
    }

    /**
     * How many time steps a cell will keep burning.
     *
     * <p>Formula: duration = vegetationDensity × resistance × BASE_BURN_DURATION</p>
     *
     * @param c the cell that just caught fire
     * @return burn duration in steps (minimum 1)
     */
    public static int burnDuration(Cell c) {
        double d = c.getVegetationDensity() * c.getResistance() * Constants.BASE_BURN_DURATION;
        return Math.max(1, (int) Math.round(d));
    }

    /**
     * Probability that a burning cell extinguishes spontaneously in one step.
     *
     * <p>Formula: P = (1 − density) × (1 − resistance) × humidity</p>
     *
     * @param c the burning cell
     * @return probability in [0.0, 1.0]
     */
    public static double extinctionProbability(Cell c) {
        return (1.0 - c.getVegetationDensity())
             * (1.0 - c.getResistance())
             * c.getHumidity();
    }
}
