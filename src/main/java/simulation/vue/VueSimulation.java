package simulation.vue;

import simulation.Simulateur;
import simulation.modele.Grille;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class VueSimulation extends JFrame {

    private final PanneauGrille    panneauGrille;
    private final PanneauControle  panneauControle;
    private final JPanel           panneauLegende;

    public VueSimulation(Simulateur simulateur) {
        super("Simulation de propagation de feu de forêt");

        panneauGrille   = new PanneauGrille(simulateur);
        panneauControle = new PanneauControle(simulateur, this);
        panneauLegende  = new LegendPanel();

        setLayout(new BorderLayout());
        add(panneauGrille,   BorderLayout.CENTER);
        add(panneauControle, BorderLayout.SOUTH);
        add(panneauLegende,  BorderLayout.EAST);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        // Allow resizing so the grid can expand to fill available space
        setResizable(true);
        // Provide a reasonable initial size larger than packed size so center fills
        setSize(1000, 800);

        // Keyboard shortcuts: + / = => zoom in, - => zoom out, 0 => reset
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke('+'), "zoomIn");
        im.put(KeyStroke.getKeyStroke('='), "zoomIn");
        im.put(KeyStroke.getKeyStroke((char)43), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");

        im.put(KeyStroke.getKeyStroke('-'), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");

        im.put(KeyStroke.getKeyStroke('0'), "resetZoom");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "resetZoom");

        am.put("zoomIn", new AbstractAction() { public void actionPerformed(ActionEvent e) { zoomIn(); } });
        am.put("zoomOut", new AbstractAction() { public void actionPerformed(ActionEvent e) { zoomOut(); } });
        am.put("resetZoom", new AbstractAction() { public void actionPerformed(ActionEvent e) { resetZoom(); } });
    }

    public void rafraichir(Grille grille) {
        panneauGrille.rafraichir(grille);
        panneauControle.rafraichirPas();
    }

    public void zoomIn() {
        panneauGrille.zoomIn();
    }

    public void zoomOut() {
        panneauGrille.zoomOut();
    }

    public void resetZoom() {
        panneauGrille.resetZoom();
    }
}
