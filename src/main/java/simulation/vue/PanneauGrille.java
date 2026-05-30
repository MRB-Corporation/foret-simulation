package simulation.vue;

import simulation.Simulateur;
import simulation.modele.Cellule;
import simulation.modele.EtatCellule;
import simulation.modele.Grille;
import simulation.utilitaire.Constantes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PanneauGrille extends JPanel {

    private Grille grille;

    public PanneauGrille(Simulateur simulateur) {
        this.grille = simulateur.getGrille();

        int w = grille.getLargeur() * Constantes.TAILLE_CELLULE_PX;
        int h = grille.getHauteur() * Constantes.TAILLE_CELLULE_PX;
        setPreferredSize(new Dimension(w, h));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int cx = e.getX() / Constantes.TAILLE_CELLULE_PX;
                int cy = e.getY() / Constantes.TAILLE_CELLULE_PX;
                simulateur.allumerFeu(cx, cy);
            }
        });
    }

    public void rafraichir(Grille grille) {
        this.grille = grille;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grille == null) return;

        int t = Constantes.TAILLE_CELLULE_PX;

        for (int y = 0; y < grille.getHauteur(); y++) {
            for (int x = 0; x < grille.getLargeur(); x++) {
                Cellule c = grille.getCellule(x, y);
                g.setColor(couleurPourCellule(c));
                g.fillRect(x * t, y * t, t, t);
            }
        }

        g.setColor(new Color(0, 0, 0, 25));
        for (int x = 0; x <= grille.getLargeur(); x++)
            g.drawLine(x * t, 0, x * t, grille.getHauteur() * t);
        for (int y = 0; y <= grille.getHauteur(); y++)
            g.drawLine(0, y * t, grille.getLargeur() * t, y * t);
    }

    private Color couleurPourCellule(Cellule c) {
        if (c.getEtat() == EtatCellule.EN_FEU) return new Color(255, 80,  0);
        if (c.getEtat() == EtatCellule.BRULE)  return new Color(50,  30,  10);
        switch (c.getTerrain()) {
            case FORET:          return new Color(34,  139, 34);
            case ZONE_HUMIDE:    return new Color(32,  178, 170);
            case COUPE_FEU:      return new Color(160, 140, 100);
            case EAU:            return new Color(30,  100, 200);
            case ZONE_URBANISEE: return new Color(180, 180, 180);
            default:             return Color.BLACK;
        }
    }
}
