package simulation.utilitaire;

import simulation.modele.Cellule;
import simulation.modele.Vent;

public final class FormulesPhysiques {

    private FormulesPhysiques() {}

    private static final double TEMPERATURE_FEU = 600.0; // °C
    private static final double CONDUCTIVITE     = 0.1;

    // ── 1. Probabilité d'inflammation de base ─────────────────────────────────
    // P = inflammabilite × (1 - humidite) × (1 - resistance) × densiteVegetation
    public static double probabiliteInflammation(Cellule c) {
        return c.getInflammabilite()
             * (1.0 - c.getHumidite())
             * (1.0 - c.getResistance())
             * c.getDensiteVegetation();
    }

    // ── 2. Facteur vent ───────────────────────────────────────────────────────
    // facteurVent = 1 + (vitesse / vitesseMax) × cos(angleVent - angleVers)
    // dx, dy : déplacement de la cellule source vers le voisin ciblé
    public static double facteurVent(Vent vent, int dx, int dy) {
        double angleVers = Math.atan2(dy, dx);
        return 1.0 + (vent.getVitesse() / Vent.VITESSE_MAX) * Math.cos(vent.getDirection() - angleVers);
    }

    // ── 3. Probabilité d'inflammation avec vent ───────────────────────────────
    public static double probabiliteInflammationAvecVent(Cellule c, Vent vent, int dx, int dy) {
        return probabiliteInflammation(c) * facteurVent(vent, dx, dy);
    }

    // ── 4. Montée en température d'un voisin chauffé par une cellule EN_FEU ──
    // delta = (temperatureFeu - temperatureVoisin) × conductivite × (1 - humidite)
    public static double deltaTemperature(Cellule voisin) {
        return (TEMPERATURE_FEU - voisin.getTemperature())
             * CONDUCTIVITE
             * (1.0 - voisin.getHumidite());
    }

    // ── 5. Durée de combustion en pas de temps ────────────────────────────────
    // duree = densiteVegetation × resistance × dureeBase
    public static int dureeCombuston(Cellule c) {
        double duree = c.getDensiteVegetation() * c.getResistance() * Constantes.DUREE_BASE_COMBUSTION;
        return Math.max(1, (int) Math.round(duree));
    }

    // ── 6. Probabilité d'extinction spontanée (alternative au compteur) ───────
    // P = (1 - densiteVegetation) × (1 - resistance) × humidite
    public static double probabiliteExtinction(Cellule c) {
        return (1.0 - c.getDensiteVegetation())
             * (1.0 - c.getResistance())
             * c.getHumidite();
    }
}
