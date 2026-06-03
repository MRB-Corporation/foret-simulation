package simulation.vue;

import simulation.Simulateur;
import simulation.algorithme.AlgorithmePropagation;
import simulation.algorithme.PropagationMoore;
import simulation.algorithme.PropagationOrthogonale;
import simulation.algorithme.PropagationRadiale;
import simulation.chargeur.ChargeurTopologie;
import simulation.export.ExporteurSimulationCsv;
import simulation.modele.Grille;
import simulation.modele.Vent;
import simulation.utilitaire.Constantes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;

public class PanneauControle extends JPanel {

    private final Simulateur simulateur;
    private final VueSimulation vue;

    private JButton btnDemarrerPause;
    private JLabel  lblPas;
    private boolean enMarche = false;

    private JComboBox<String> comboVent;
    private JSlider sliderVent;

    // "Vent du Nord" = souffle vers le Sud = direction π/2 en coords grille (y vers le bas)
    // "Vent de l'Ouest" = souffle vers l'Est = direction 0
    private static final double[] DIRECTIONS_VENT = {
        0.0,              // Aucun (vitesse forcée à 0)
        Math.PI / 2,      // Vent du Nord (souffle vers le Sud)
        -Math.PI / 2,     // Vent du Sud  (souffle vers le Nord)
        0.0,              // Vent de l'Ouest (souffle vers l'Est)
        Math.PI           // Vent de l'Est  (souffle vers l'Ouest)
    };

    private static final AlgorithmePropagation[] ALGOS = {
        new PropagationOrthogonale(),
        new PropagationMoore(),
        new PropagationRadiale()
    };

    public PanneauControle(Simulateur simulateur, VueSimulation vue) {
        this.simulateur = simulateur;
        this.vue = vue;

        setLayout(new WrapLayout(FlowLayout.CENTER, 10, 8));
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        setBackground(Color.WHITE);
        setOpaque(true);

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

        // --- Zoom controls ---
        JButton btnZoomIn = new JButton("Zoom +");
        btnZoomIn.setToolTipText("Zoomer (touche +)");
        btnZoomIn.addActionListener(e -> vue.zoomIn());
        add(btnZoomIn);

        JButton btnZoomOut = new JButton("Zoom -");
        btnZoomOut.setToolTipText("Dézoomer (touche -)");
        btnZoomOut.addActionListener(e -> vue.zoomOut());
        add(btnZoomOut);

        JButton btnResetZoom = new JButton("Réinitialiser zoom");
        btnResetZoom.setToolTipText("Réinitialise le zoom (touche 0)");
        btnResetZoom.addActionListener(e -> vue.resetZoom());
        add(btnResetZoom);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Slider vitesse simulation ---
        add(new JLabel("Vitesse :"));
        JSlider sliderVitesse = new JSlider(
            Constantes.VITESSE_MIN_MS, Constantes.VITESSE_MAX_MS, Constantes.VITESSE_DEFAUT_MS
        );
        sliderVitesse.setInverted(true);
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

        // --- Vent ---
        add(new JLabel("Vent :"));
        comboVent = new JComboBox<>(new String[]{
            "Aucun", "Du Nord", "Du Sud", "De l'Ouest", "De l'Est"
        });
        comboVent.addActionListener(e -> appliquerVent());
        add(comboVent);

        sliderVent = new JSlider(0, 100, 0);
        sliderVent.setToolTipText("Intensité du vent");
        sliderVent.setPreferredSize(new Dimension(100, 40));
        sliderVent.addChangeListener(e -> appliquerVent());
        add(sliderVent);

        add(new JSeparator(JSeparator.VERTICAL));

        // --- Charger / Sauvegarder ---
        JButton btnCharger = new JButton("Charger CSV");
        btnCharger.addActionListener(e -> chargerFichier());
        add(btnCharger);

        JButton btnSauvegarder = new JButton("Sauvegarder CSV");
        btnSauvegarder.addActionListener(e -> sauvegarderFichier());
        add(btnSauvegarder);

        JButton btnExporterSimulation = new JButton("Exporter simulation CSV");
        btnExporterSimulation.setToolTipText("Exporte les statistiques de la simulation démarrée");
        btnExporterSimulation.addActionListener(e -> exporterSimulation());
        add(btnExporterSimulation);

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

    private void appliquerVent() {
        int idx = comboVent.getSelectedIndex();
        double vitesse = (idx == 0) ? 0.0 : sliderVent.getValue() / 100.0;
        simulateur.setVent(new Vent(DIRECTIONS_VENT[idx], vitesse));
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

    private void exporterSimulation() {
        if (simulateur.getHistoriqueSimulation().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Démarre d'abord la simulation pour créer des données à exporter.",
                "Aucune donnée de simulation",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
        fc.setSelectedFile(new java.io.File("simulation_donnees.csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String chemin = fc.getSelectedFile().getAbsolutePath();
            if (!chemin.endsWith(".csv")) chemin += ".csv";

            try {
                ExporteurSimulationCsv.exporter(simulateur.getHistoriqueSimulation(), chemin);
                JOptionPane.showMessageDialog(
                    this,
                    "Export terminé : " + simulateur.getHistoriqueSimulation().size() + " lignes de données.",
                    "CSV exporté",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Impossible d'exporter le CSV : " + ex.getMessage(),
                    "Erreur d'export",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
