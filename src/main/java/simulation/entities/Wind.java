package simulation.entities;

/**
 * Represents the wind: a direction (radians) and a speed normalised to [0.0, MAX_SPEED].
 *
 * <p>Convention: angle 0 = East, π/2 = South, π = West, −π/2 = North.</p>
 */
public class Wind {

    /** Maximum allowed wind speed (normalised). */
    public static final double MAX_SPEED = 1.0;

    /** Wind direction in radians. */
    private double direction;

    /** Wind speed in [0.0, MAX_SPEED]. */
    private double speed;

    /** Creates a wind with no movement. */
    public Wind() {
        this.direction = 0.0;
        this.speed = 0.0;
    }

    /**
     * Creates a wind with the given direction and speed.
     *
     * @param direction angle in radians
     * @param speed     normalised speed (clamped to [0, MAX_SPEED])
     */
    public Wind(double direction, double speed) {
        this.direction = direction;
        setSpeed(speed);
    }

    /** @return direction in radians */
    public double getDirection() { return direction; }

    /** @return speed in [0.0, MAX_SPEED] */
    public double getSpeed() { return speed; }

    /** @param direction new direction in radians */
    public void setDirection(double direction) { this.direction = direction; }

    /**
     * Sets speed, clamped to [0.0, MAX_SPEED].
     * @param v new speed value
     */
    public void setSpeed(double v) {
        if (v < 0.0) v = 0.0;
        if (v > MAX_SPEED) v = MAX_SPEED;
        this.speed = v;
    }
}
