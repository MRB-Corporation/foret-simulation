package simulation.algorithme;

import simulation.modele.Cellule;
import simulation.modele.Grille;
import java.util.ArrayList;
import java.util.List;

public class PropagationMoore implements AlgorithmePropagation {

    @Override
    public List<Cellule> calculerVoisins(Cellule cellule, Grille grille) {
        List<Cellule> voisins = new ArrayList<>();

        int x = cellule.getX();
        int y = cellule.getY();

        // Les 8 voisins : directs + diagonales
        int[][] directions = {
            { 0, -1},  // Nord
            { 0,  1},  // Sud
            { 1,  0},  // Est
            {-1,  0},  // Ouest
            { 1, -1},  // Nord-Est
            {-1, -1},  // Nord-Ouest
            { 1,  1},  // Sud-Est
            {-1,  1}   // Sud-Ouest
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (grille.estValide(nx, ny)) {
                Cellule voisin = grille.getCellule(nx, ny);
                if (voisin.peutSEnflammer()) {
                    voisins.add(voisin);
                }
            }
        }

        return voisins;
    }

    @Override
    public String getNom() {
        return "Propagation Moore";
    }
}
// TODO
