package simulation.algorithme;

import simulation.modele.Cellule;
import simulation.modele.Grille;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PropagationRadiale implements AlgorithmePropagation {

    private int rayon;
    private static final Random RNG = new Random();

    public PropagationRadiale(int rayon) {
        this.rayon = rayon;
    }

    public PropagationRadiale() {
        this(3);
    }

    @Override
    public List<Cellule> calculerVoisins(Cellule cellule, Grille grille) {
        List<Cellule> voisins = new ArrayList<>();

        int x = cellule.getX();
        int y = cellule.getY();

        for (int dx = -rayon; dx <= rayon; dx++) {
            for (int dy = -rayon; dy <= rayon; dy++) {

                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 0 && distance <= rayon) {
                    // Atténuation avec la distance : P(inclusion) = 1 / distance²
                    // d=1 → 100%, d=√2 → 50%, d=2 → 25%, d=3 → 11%
                    if (RNG.nextDouble() > 1.0 / (distance * distance)) continue;

                    int nx = x + dx;
                    int ny = y + dy;

                    if (grille.estValide(nx, ny)) {
                        Cellule voisin = grille.getCellule(nx, ny);
                        if (voisin.peutSEnflammer()) {
                            voisins.add(voisin);
                        }
                    }
                }
            }
        }

        return voisins;
    }

    @Override
    public String getNom() {
        return "Propagation Radiale (rayon " + rayon + ")";
    }

    public int getRayon() { return rayon; }
    public void setRayon(int rayon) { this.rayon = rayon; }
}
