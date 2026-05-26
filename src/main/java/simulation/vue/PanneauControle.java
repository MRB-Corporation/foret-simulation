package simulation.vue;

import simulation.Simulateur;
import simulation.algorithme.AlgorithmePropagation;
import simulation.algorithme.PropagationMoore;
import simulation.algorithme.PropagationOrthogonale;
import simulation.algorithme.PropagationRadiale;
import simulation.chargeur.ChargeurTopologie;
import simulation.modele.Grille;
import simulation.utilitaire.Constantes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class PanneauControle extends JPanel {

    private final Simulateur simulateur;
    private final VueSimulation vue;

    private JButton btnDemarrerPause;
    private JLabel  lblPas;
    private boolean enMarche = false;

    private static final AlgorithmePropagation[] ALGOS = {
        new PropagationOrthogonale(),
        new PropagationMoore(),
        new PropagationRadiale()
    };

    public PanneauControle(Simulateur simulateur, VueSimulation vue) {
        this.simulateur = simulateur;
        this.vue = vue;

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        construire();
    }

    private void construire() {
        // --- Démarrer / Pause ---
        btnDemarrerPause = new JButton("Démarrer");
        btnDemarrerPause.addActionListener(e -> toggleDemarrerPause());
        add(btnDemarrerPause);

        // --- Reset ---
        JButton btnReset = new JButton("Réinitialiser");
        btnReset.addActionListener(e -> {
            simulateur.reset();
            enMarche = false;
            btnDemarrerPause.setText("Démarrer");
        });
        add(btnReset);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Slider vitesse ---
        add(new JLabel("Vitesse :"));
        JSlider sliderVitesse = new JSlider(
            Constantes.VITESSE_MIN_MS, Constantes.VITESSE_MAX_MS, Constantes.VITESSE_DEFAUT_MS
        );
        sliderVitesse.setInverted(true); // valeur haute = lent → on inverse pour que droite = rapide
        sliderVitesse.setMajorTickSpacing(500);
        sliderVitesse.setPaintTicks(true);
        sliderVitesse.setPreferredSize(new Dimension(150, 40));
        sliderVitesse.addChangeListener(e -> simulateur.setVitesse(sliderVitesse.getValue()));
        add(sliderVitesse);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Sélecteur algorithme ---
        add(new JLabel("Algorithme :"));
        String[] noms = new String[ALGOS.length];
        for (int i = 0; i < ALGOS.length; i++) noms[i] = ALGOS[i].getNom();
        JComboBox<String> comboAlgo = new JComboBox<>(noms);
        comboAlgo.addActionListener(e -> simulateur.setAlgorithme(ALGOS[comboAlgo.getSelectedIndex()]));
        add(comboAlgo);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Charger / Sauvegarder ---
        JButton btnCharger = new JButton("Charger CSV");
        btnCharger.addActionListener(e -> chargerFichier());
        add(btnCharger);

        JButton btnSauvegarder = new JButton("Sauvegarder CSV");
        btnSauvegarder.addActionListener(e -> sauvegarderFichier());
        add(btnSauvegarder);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Compteur de pas ---
        lblPas = new JLabel("Pas : 0");
        add(lblPas);
    }

    // ── Contrôles ──────────────────────────────────────────────────────────────

    private void toggleDemarrerPause() {
        if (enMarche) {
            simulateur.pause();
            btnDemarrerPause.setText("Démarrer");
        } else {
            simulateur.demarrer();
            btnDemarrerPause.setText("Pause");
        }
        enMarche = !enMarche;
    }

    public void rafraichirPas() {
        lblPas.setText("Pas : " + simulateur.getPas());
    }

    // ── Fichiers ───────────────────────────────────────────────────────────────

    private void chargerFichier() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Grille g = ChargeurTopologie.chargerFichier(fc.getSelectedFile().getAbsolutePath());
            simulateur.chargerGrille(g);
            enMarche = false;
            btnDemarrerPause.setText("Démarrer");
            lblPas.setText("Pas : 0");
        }
    }

    private void sauvegarderFichier() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String chemin = fc.getSelectedFile().getAbsolutePath();
            if (!chemin.endsWith(".csv")) chemin += ".csv";
            ChargeurTopologie.sauvegarder(simulateur.getGrille(), chemin);
        }
    }
}
