package simulation.modele;

public class Vent {

    public enum Direction { NORD, SUD, EST, OUEST, AUCUN }

    private Direction direction;
    private double force; // 0.0 à 1.0

    public Vent() {
        this.direction = Direction.AUCUN;
        this.force = 0.0;
    }

    public Vent(Direction direction, double force) {
        this.direction = direction;
        setForce(force);
    }

    public Direction getDirection() { return direction; }
    public double getForce()        { return force; }

    public void setDirection(Direction direction) { this.direction = direction; }

    public void setForce(double force) {
        if (force < 0.0) force = 0.0;
        if (force > 1.0) force = 1.0;
        this.force = force;
    }
}
