package simulation.algorithme;

import simulation.modele.Cellule;
import simulation.modele.Grille;
import java.util.List;

public interface AlgorithmePropagation {

    // Retourne la liste des voisins concernés par la propagation
    List<Cellule> calculerVoisins(Cellule cellule, Grille grille);

    // Nom de l'algo affiché dans l'interface graphique
    String getNom();
}
// TODO
