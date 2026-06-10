package simulation.modele;

public class Cellule implements ComportementFeu {

    private int x;
    private int y;
    private EtatCellule etat;
    private TypeTerrain terrain;
    private double humidite;          // 0.0 à 1.0
    private double inflammabilite;    // 0.0 à 1.0
    private double resistance;        // 0.0 à 1.0
    private double densiteVegetation; // 0.0 à 1.0
    private double temperature;       // en °C
    private int compteurCombuston;    // pas restants avant extinction

    public Cellule(int x, int y) {
        this.x = x;
        this.y = y;
        this.etat = EtatCellule.INTACT;
        this.terrain = TypeTerrain.FORET;
        this.humidite = 0.30;
        this.inflammabilite = 0.50;
        this.resistance = 0.50;
        this.densiteVegetation = 1.0;
        this.temperature = 20.0;
        this.compteurCombuston = 0;
    }

    // ── ComportementFeu ────────────────────────────────────────────────────────

    @Override
    public boolean peutSEnflammer() {
        return etat == EtatCellule.INTACT && !stoppeLesFeu();
    }

    @Override
    public boolean stoppeLesFeu() {
        return terrain == TypeTerrain.COUPE_FEU
            || terrain == TypeTerrain.EAU
            || terrain == TypeTerrain.ZONE_HUMIDE
            || terrain == TypeTerrain.ZONE_URBANISEE;
    }

    @Override
    public void chauffer(double delta) {
        temperature += delta;
    }

    @Override
    public void evoluerHumidite(double delta) {
        humidite = Math.max(0.0, Math.min(1.0, humidite + delta));
    }

    public void decrementerCombuston() {
        if (compteurCombuston > 0) compteurCombuston--;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public int getX()                    { return x; }
    public int getY()                    { return y; }
    public EtatCellule getEtat()         { return etat; }
    public TypeTerrain getTerrain()      { return terrain; }
    public double getHumidite()          { return humidite; }
    public double getInflammabilite()    { return inflammabilite; }
    public double getResistance()        { return resistance; }
    public double getDensiteVegetation() { return densiteVegetation; }
    public double getTemperature()       { return temperature; }
    public int getCompteurCombuston()    { return compteurCombuston; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setEtat(EtatCellule etat)    { this.etat = etat; }
    public void setTerrain(TypeTerrain t)    { this.terrain = t; }
    public void setTemperature(double v)     { this.temperature = v; }
    public void setCompteurCombuston(int v)  { this.compteurCombuston = v; }

    public void setHumidite(double v) {
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        this.humidite = v;
    }

    public void setInflammabilite(double v) {
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        this.inflammabilite = v;
    }

    public void setResistance(double v) {
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        this.resistance = v;
    }

    public void setDensiteVegetation(double v) {
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        this.densiteVegetation = v;
    }
}
