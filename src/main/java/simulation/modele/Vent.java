package simulation.modele;

public class Vent {

    // Convention : angle en radians, direction vers laquelle souffle le vent
    // 0 = vers l'Est, π/2 = vers le Sud, π = vers l'Ouest, -π/2 = vers le Nord
    public static final double VITESSE_MAX = 1.0;

    private double direction; // radians
    private double vitesse;   // 0.0 à VITESSE_MAX

    public Vent() {
        this.direction = 0.0;
        this.vitesse = 0.0;
    }

    public Vent(double direction, double vitesse) {
        this.direction = direction;
        setVitesse(vitesse);
    }

    public double getDirection() { return direction; }
    public double getVitesse()   { return vitesse; }

    public void setDirection(double direction) { this.direction = direction; }

    public void setVitesse(double v) {
        if (v < 0.0) v = 0.0;
        if (v > VITESSE_MAX) v = VITESSE_MAX;
        this.vitesse = v;
    }
}
