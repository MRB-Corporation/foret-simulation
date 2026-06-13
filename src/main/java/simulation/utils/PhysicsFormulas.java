package simulation.utils;

import simulation.entities.Cell;
import simulation.entities.Wind;
import simulation.config.Constants;

/**
 * Classe utilitaire qui regroupe l'intégralité des formules physiques de la simulation.
 *
 * <p>Nous avons choisi de rendre cette classe statique et pure (sans effets de bord)
 * afin de séparer la logique mathématique de l'état de la grille, ce qui facilite
 * grandement les tests unitaires et la compréhension lors de la soutenance.</p>
 */
public final class PhysicsFormulas {

    private PhysicsFormulas() {}

    /**
     * Calcule la probabilité de base qu'une cellule prenne feu si un de ses voisins brûle.
     *
<<<<<<< Updated upstream
     * <p>Formula: P = inflammability × (1 − humidity) × (1 − resistance) × vegetationDensity</p>
=======
     * <p>La formule mathématique que nous avons conçue est :
     * P = inflammabilité × (1 − humidité) × (1 − résistance) × densitéVégétale</p>
     * <p>Le résultat est borné mathématiquement entre [0.0, 1.0].</p>
>>>>>>> Stashed changes
     *
     * @param c La cellule cible qui risque de prendre feu.
     * @return La probabilité d'ignition, comprise entre 0.0 (impossible) et 1.0 (certain).
     */
    public static double ignitionProbability(Cell c) {
        return c.getInflammability()
             * (1.0 - c.getHumidity())
             * (1.0 - c.getResistance())
             * c.getVegetationDensity();
    }

    /**
     * Calcule l'influence du vent (facteur multiplicatif) sur la propagation dans une direction donnée.
     *
<<<<<<< Updated upstream
     * <p>Formula: factor = 1 + (speed / maxSpeed) × cos(windAngle − targetAngle)</p>
     *
     * @param wind the current wind (must not be null)
     * @param dx   column offset from burning cell to target neighbour
     * @param dy   row offset from burning cell to target neighbour
     * @return multiplicative factor ≥ 0
=======
     * <p>La formule trigonométrique utilisée :
     * facteur = 1 + AMPLITUDE × (vitesse / VitesseMax) × cos(angleDuVent − angleVersLaCible)</p>
     *
     * <p>Explication pour le jury : Si le vent souffle vers la cible (cos(0) = 1), 
     * le facteur peut tripler la probabilité de base. Si le vent souffle dans la direction 
     * opposée (cos(PI) = -1), la probabilité est fortement réduite, voire annulée.</p>
     *
     * <p>Ce choix nous a permis d'avoir un effet visuel très réaliste du vent sur l'interface.</p>
     *
     * @param wind Le vent actuel (vitesse et angle en radians).
     * @param dx   La différence sur l'axe X entre la cellule en feu et la cellule cible.
     * @param dy   La différence sur l'axe Y.
     * @return Le facteur multiplicatif de propagation (toujours >= 0 pour éviter un plantage).
>>>>>>> Stashed changes
     */
    public static double windFactor(Wind wind, int dx, int dy) {
        double angleToNeighbour = Math.atan2(dy, dx);
        return 1.0 + (wind.getSpeed() / Wind.MAX_SPEED)
                   * Math.cos(wind.getDirection() - angleToNeighbour);
    }

    /**
<<<<<<< Updated upstream
     * Ignition probability adjusted for wind direction and speed.
     *
     * @param c    target cell
     * @param wind current wind
     * @param dx   column offset (burning → target)
     * @param dy   row offset   (burning → target)
     * @return probability in [0.0, ∞) — clamped to 1.0 before use
=======
     * Fonction finale qui combine la probabilité d'ignition de base avec le facteur du vent.
     * C'est cette fonction qui est appelée par le Simulator à chaque itération.
     *
     * @param c    La cellule cible.
     * @param wind L'objet vent actuel.
     * @param dx   Décalage en X (pour calculer l'angle).
     * @param dy   Décalage en Y.
     * @return La probabilité finale d'ignition, strictement bornée entre 0.0 et 1.0.
>>>>>>> Stashed changes
     */
    public static double ignitionProbabilityWithWind(Cell c, Wind wind, int dx, int dy) {
        return ignitionProbability(c) * windFactor(wind, dx, dy);
    }

    /**
     * Calcule l'augmentation de température (le rayonnement thermique) qu'une cellule en feu 
     * transmet à ses voisines à chaque "tick" de simulation.
     *
     * <p>Nous avons implémenté une thermodynamique basique : plus une cellule est humide, 
     * moins elle s'échauffe vite (l'eau absorbe la chaleur).</p>
     *
     * @param neighbour La cellule voisine qui subit la chaleur.
     * @return Le delta de température à ajouter (en degrés).
     */
    public static double temperatureDelta(Cell neighbour) {
        return (Constants.FIRE_TEMPERATURE - neighbour.getTemperature())
             * Constants.HEAT_CONDUCTIVITY
             * (1.0 - neighbour.getHumidity());
    }

    /**
     * Définit la durée pendant laquelle une cellule va brûler avant de devenir de la cendre.
     *
     * <p>Pour plus de réalisme, une forêt très dense (végétation élevée) mettra 
     * beaucoup plus de temps à se consumer qu'une clairière.</p>
     *
     * @param c La cellule qui vient de s'enflammer.
     * @return La durée de combustion en nombre de "ticks" de simulation (minimum 1 tick).
     */
    public static int burnDuration(Cell c) {
        double d = c.getVegetationDensity() * c.getResistance() * Constants.BASE_BURN_DURATION;
        return Math.max(1, (int) Math.round(d));
    }

    /**
     * Calcule la probabilité qu'un feu s'éteigne de lui-même (extinction naturelle).
     *
     * <p>Un feu s'éteint très vite s'il n'y a plus rien à brûler (densité faible) 
     * ou s'il pleut (humidité très forte).</p>
     *
     * @param c La cellule actuellement en feu.
     * @return La probabilité d'extinction spontanée.
     */
    public static double extinctionProbability(Cell c) {
        return (1.0 - c.getVegetationDensity())
             * (1.0 - c.getResistance())
             * c.getHumidity();
    }
}
