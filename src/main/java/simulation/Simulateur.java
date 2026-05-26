package simulation;

import simulation.algorithme.AlgorithmePropagation;
import simulation.algorithme.PropagationOrthogonale;
import simulation.chargeur.ChargeurTopologie;
import simulation.modele.Cellule;
import simulation.modele.EtatCellule;
import simulation.modele.Grille;
import simulation.utilitaire.Constantes;
import simulation.utilitaire.FormulesPhysiques;
import simulation.vue.VueSimulation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulateur {

    private Grille grille;
    private Grille grilleInitiale;
    private AlgorithmePropagation algorithme;
    private VueSimulation vue;
    private Timer timer;
    private int vitesseMs;
    private int pas;

    private static final Random RNG = new Random();

    public Simulateur() {
        vitesseMs = Constantes.VITESSE_DEFAUT_MS;
        pas = 0;
        algorithme = new PropagationOrthogonale();
        grille = ChargeurTopologie.genererAleatoire();
        grilleInitiale = copierGrille(grille);
    }

    // ── Contrôle ───────────────────────────────────────────────────────────────

    public void demarrer() {
        if (timer == null) {
            timer = new Timer(vitesseMs, e -> {
                pasDeTemps();
                if (vue != null) vue.rafraichir(grille);
            });
        }
        timer.setDelay(vitesseMs);
        timer.start();
    }

    public void pause() {
        if (timer != null) timer.stop();
    }

    public void reset() {
        pause();
        pas = 0;
        grille = copierGrille(grilleInitiale);
        if (vue != null) vue.rafraichir(grille);
    }

    // ── Logique de propagation ─────────────────────────────────────────────────

    public void pasDeTemps() {
        pas++;
        List<Cellule> aEnflammer = new ArrayList<>();
        List<Cellule> aBruler    = new ArrayList<>();

        for (int y = 0; y < grille.getHauteur(); y++) {
            for (int x = 0; x < grille.getLargeur(); x++) {
                Cellule c = grille.getCellule(x, y);
                if (c.getEtat() != EtatCellule.EN_FEU) continue;

                // Propagation vers les voisins inflammables
                for (Cellule voisin : algorithme.calculerVoisins(c, grille)) {
                    if (RNG.nextDouble() < FormulesPhysiques.probabiliteInflammation(voisin)) {
                        aEnflammer.add(voisin);
                    }
                }

                // La cellule se consume (haute résistance = brûle lentement)
                if (RNG.nextDouble() > c.getResistance()) {
                    aBruler.add(c);
                }
            }
        }

        for (Cellule c : aEnflammer) {
            if (c.getEtat().peutSEnflammer()) c.setEtat(EtatCellule.EN_FEU);
        }
        for (Cellule c : aBruler) {
            c.setEtat(EtatCellule.BRULE);
        }
    }

    // ── Interaction utilisateur ────────────────────────────────────────────────

    public void allumerFeu(int x, int y) {
        Cellule c = grille.getCellule(x, y);
        if (c != null && c.getEtat().peutSEnflammer()) {
            c.setEtat(EtatCellule.EN_FEU);
            if (vue != null) vue.rafraichir(grille);
        }
    }

    public void chargerGrille(Grille nouvelleGrille) {
        pause();
        pas = 0;
        grille = nouvelleGrille;
        grilleInitiale = copierGrille(grille);
        if (vue != null) vue.rafraichir(grille);
    }

    // ── Utilitaire ─────────────────────────────────────────────────────────────

    private Grille copierGrille(Grille source) {
        Grille copie = new Grille(source.getLargeur(), source.getHauteur());
        for (int y = 0; y < source.getHauteur(); y++) {
            for (int x = 0; x < source.getLargeur(); x++) {
                Cellule src = source.getCellule(x, y);
                Cellule dst = copie.getCellule(x, y);
                dst.setEtat(src.getEtat());
                dst.setHumidite(src.getHumidite());
                dst.setInflammabilite(src.getInflammabilite());
                dst.setResistance(src.getResistance());
                dst.setDensiteVegetation(src.getDensiteVegetation());
                dst.setTemperature(src.getTemperature());
            }
        }
        return copie;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Grille getGrille()  { return grille; }
    public int getPas()        { return pas; }

    public void setVue(VueSimulation vue) {
        this.vue = vue;
        vue.rafraichir(grille);
    }

    public void setAlgorithme(AlgorithmePropagation algo) {
        this.algorithme = algo;
    }

    public void setVitesse(int ms) {
        vitesseMs = ms;
        if (timer != null) timer.setDelay(ms);
    }

    // ── Point d'entrée ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Simulateur sim = new Simulateur();
            VueSimulation vue = new VueSimulation(sim);
            sim.setVue(vue);
            vue.setVisible(true);
        });
    }
}
