package simulation.vue;

import simulation.Simulateur;
import simulation.modele.Grille;

import javax.swing.*;
import java.awt.*;

public class VueSimulation extends JFrame {

    private final PanneauGrille    panneauGrille;
    private final PanneauControle  panneauControle;

    public VueSimulation(Simulateur simulateur) {
        super("Simulation de propagation de feu de forêt");

        panneauGrille   = new PanneauGrille(simulateur);
        panneauControle = new PanneauControle(simulateur, this);

        setLayout(new BorderLayout());
        add(panneauGrille,   BorderLayout.CENTER);
        add(panneauControle, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void rafraichir(Grille grille) {
        panneauGrille.rafraichir(grille);
        panneauControle.rafraichirPas();
    }
}
