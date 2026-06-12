package simulation.utils;

import simulation.entities.Cell;
import simulation.entities.Wind;
import simulation.config.Constants;

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
     * <p>Result is clamped to [0.0, 1.0].</p>
     *
     * @param c the candidate cell (must not be null)
     * @return probability in [0.0, 1.0]
     */
    public static double ignitionProbability(Cell c) {
        double p = c.getInflammability()
                 * (1.0 - c.getHumidity())
                 * (1.0 - c.getResistance())
                 * c.getVegetationDensity();
        return Math.max(0.0, Math.min(1.0, p));
    }

    /**
     * Wind factor that amplifies or reduces fire spread toward a given direction.
     *
     * <p>Formula: factor = 1 + windAmplitude × (speed / maxSpeed) × cos(windAngle − targetAngle)</p>
     *
     * <p>A WIND_AMPLITUDE of 2.0 means the wind can triple the probability in its
     * direction and reduce it to near zero against it — making the effect clearly
     * visible in the simulation.</p>
     *
     * <p>Result is always ≥ 0 (clamped) so probability never goes negative.</p>
     *
     * @param wind the current wind (must not be null)
     * @param dx   column offset from burning cell to target neighbour
     * @param dy   row offset from burning cell to target neighbour
     * @return multiplicative factor in [0.0, 1 + WIND_AMPLITUDE]
     */
    public static double windFactor(Wind wind, int dx, int dy) {
        if (wind.getSpeed() == 0.0) return 1.0;

        // WIND_AMPLITUDE controls how strongly the wind biases propagation.
        // 2.0 → factor ranges from ~0 (against wind) to ~3 (with wind)
        final double WIND_AMPLITUDE = 2.0;

        double angleToNeighbour = Math.atan2(dy, dx);
        double factor = 1.0 + WIND_AMPLITUDE
                * (wind.getSpeed() / Wind.MAX_SPEED)
                * Math.cos(wind.getDirection() - angleToNeighbour);

        // Clamp to avoid negative probabilities
        return Math.max(0.0, factor);
    }

    /**
     * Ignition probability adjusted for wind direction and speed.
     * Result is clamped to [0.0, 1.0].
     *
     * @param c    target cell
     * @param wind current wind
     * @param dx   column offset (burning → target)
     * @param dy   row offset   (burning → target)
     * @return probability in [0.0, 1.0]
     */
    public static double ignitionProbabilityWithWind(Cell c, Wind wind, int dx, int dy) {
        double p = ignitionProbability(c) * windFactor(wind, dx, dy);
        return Math.max(0.0, Math.min(1.0, p));
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
