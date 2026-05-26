package simulation.utilitaire;

import simulation.modele.Cellule;

public final class FormulesPhysiques {

    private FormulesPhysiques() {}

    /**
     * P = inflammabilite × (1 - humidite) × (1 - resistance) × densiteVegetation
     * Résultat entre 0.0 et 1.0.
     */
    public static double probabiliteInflammation(Cellule c) {
        return c.getInflammabilite()
             * (1.0 - c.getHumidite())
             * (1.0 - c.getResistance())
             * c.getDensiteVegetation();
    }
}
