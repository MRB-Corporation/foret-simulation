package simulation.algorithme;

import simulation.modele.Cellule;
import simulation.modele.Grille;
import java.util.ArrayList;
import java.util.List;

public class PropagationRadiale implements AlgorithmePropagation {

    private int rayon;

    public PropagationRadiale(int rayon) {
        this.rayon = rayon;
    }

    // Constructeur par défaut avec rayon de 3
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

                // On calcule la distance euclidienne
                double distance = Math.sqrt(dx * dx + dy * dy);

                // On garde uniquement les cellules dans le cercle
                // et on exclut la cellule elle-même (distance 0)
                if (distance > 0 && distance <= rayon) {
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
// TODO
