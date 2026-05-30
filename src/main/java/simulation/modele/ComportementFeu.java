package simulation.modele;

public interface ComportementFeu {

    boolean peutSEnflammer();

    boolean stoppeLesFeu();

    void chauffer(double delta);

    void evoluerHumidite(double delta);
}
