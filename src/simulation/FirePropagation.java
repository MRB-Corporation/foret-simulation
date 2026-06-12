package simulation;

import config.SimulationConfig;
import model.Cell;
import model.CellState;
import model.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contient l'algorithme de propagation du feu.
 *
 * Formule de probabilité d'inflammation :
 *   P = (1 - humidité) × (température / température_ignition)
 *
 * Plus une cellule est sèche et chaude, plus elle a de chances de brûler.
 */
public class FirePropagation {

    /** Générateur aléatoire pour les tirages de probabilité. */
    private static final Random RANDOM = new Random();

    /**
     * Applique un pas de propagation sur toute la grille.
     *
     * Pour chaque cellule EN FEU :
     *   1. Met à jour ses voisins (baisse humidité, augmente température)
     *   2. Calcule la probabilité d'inflammation de chaque voisin
     *   3. Tire au sort pour savoir s'ils s'enflamment
     *   4. Avance la combustion de la cellule
     *
     * Les nouvelles cellules en feu sont enflammées à la fin du pas,
     * pour éviter qu'elles propagent déjà au même pas (double buffering).
     *
     * @param grid   la grille de simulation
     * @param config la configuration
     */
    public void propagate(Grid grid, SimulationConfig config) {

        // Liste des cellules qui vont s'enflammer à la fin du pas
        List<Cell> toIgnite = new ArrayList<>();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell cell = grid.getCell(x, y);

                // On ne traite que les cellules en feu
                if (cell.getState() != CellState.BURNING) continue;

                for (Cell neighbor : grid.getNeighbors(cell, config)) {

                    // Étape 1 : la chaleur du feu modifie le voisin
                    neighbor.decreaseHumidity(config.humidityDrop);
                    neighbor.increaseTemperature(config.heatTransfer);

                    // Étape 2 : probabilité d'inflammation
                    double probability = computeProbability(neighbor, config);

                    // Étape 3 : tirage au sort
                    if (neighbor.canIgnite() && RANDOM.nextDouble() < probability) {
                        toIgnite.add(neighbor);
                    }
                }

                // Étape 4 : la cellule continue de brûler
                cell.burnStep();
            }
        }

        // On enflamme toutes les cellules candidates en une seule fois
        for (Cell cell : toIgnite) {
            if (cell.canIgnite()) {
                cell.ignite(config);
            }
        }
    }

    /**
     * Calcule la probabilité qu'une cellule s'enflamme.
     *
     * Formule : P = (1 - humidité) × (température / température_ignition)
     *
     * Exemples :
     *   humidité=0.0, température=100°C, ignition=100°C → P = 1.0 (100%)
     *   humidité=0.5, température=50°C,  ignition=100°C → P = 0.25 (25%)
     *   humidité=1.0 → P = 0.0 (jamais)
     *
     * @param cell   la cellule candidate
     * @param config la configuration
     * @return probabilité entre 0.0 et 1.0
     */
    public double computeProbability(Cell cell, SimulationConfig config) {
        double humidityFactor    = 1.0 - cell.getHumidity();
        double temperatureFactor = cell.getTemperature() / config.ignitionTemperature;

        double probability = humidityFactor * temperatureFactor;

        // On borne le résultat entre 0.0 et 1.0
        return Math.max(0.0, Math.min(1.0, probability));
    }
}