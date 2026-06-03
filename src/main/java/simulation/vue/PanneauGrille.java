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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class PanneauGrille extends JPanel {

    private Grille grille;
    private int tailleCellule;
    private static final int TAILLE_MIN = 2;
    private static final int TAILLE_MAX = 80;
    private static final int MARGE_DEFILEMENT = 45;
    private static final int AGRANDISSEMENT_PAR_BORD = 25;
    // last rendered cell size used during paintComponent -> used for input mapping
    private volatile int lastRenderedCellSize;
    private int offsetX;
    private int offsetY;
    private Point positionSouris;
    private final Timer timerDefilement;

    public PanneauGrille(Simulateur simulateur) {
        this.grille = simulateur.getGrille();

        this.tailleCellule = Constantes.TAILLE_CELLULE_PX;
        // Do not set a fixed preferred size: let the layout expand the panel.
        setBackground(new Color(35, 35, 35));

        MouseAdapter souris = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int t = (lastRenderedCellSize > 0) ? lastRenderedCellSize : tailleCellule;
                int cx = offsetX + e.getX() / t;
                int cy = offsetY + e.getY() / t;
                garantirCelluleVisible(cx, cy);
                simulateur.allumerFeu(cx, cy);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                positionSouris = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                positionSouris = e.getPoint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                positionSouris = null;
            }
        };
        addMouseListener(souris);
        addMouseMotionListener(souris);

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) zoomIn(); else zoomOut();
            }
        });

        timerDefilement = new Timer(30, e -> defilerSelonSouris());
        timerDefilement.start();
    }

    public void rafraichir(Grille grille) {
        this.grille = grille;
        bornerCamera();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grille == null) return;

        int availableW = getWidth();
        int availableH = getHeight();

        garantirVueRemplie();
        bornerCamera();

        int t = getTailleCelluleRendue();
        lastRenderedCellSize = t;

        int colonnesVisibles = getColonnesVisibles();
        int lignesVisibles = getLignesVisibles();
        int finX = Math.min(grille.getLargeur(), offsetX + colonnesVisibles);
        int finY = Math.min(grille.getHauteur(), offsetY + lignesVisibles);
        int colonnesReelles = Math.max(0, finX - offsetX);
        int lignesReelles = Math.max(0, finY - offsetY);

        for (int y = offsetY; y < finY; y++) {
            for (int x = offsetX; x < finX; x++) {
                Cellule c = grille.getCellule(x, y);
                g.setColor(couleurPourCellule(c));
                g.fillRect((x - offsetX) * t, (y - offsetY) * t, t, t);
            }
        }

        g.setColor(new Color(0, 0, 0, 25));
        int largeurCarte = colonnesReelles * t;
        int hauteurCarte = lignesReelles * t;
        for (int x = 0; x <= colonnesReelles; x++)
            g.drawLine(x * t, 0, x * t, hauteurCarte);
        for (int y = 0; y <= lignesReelles; y++)
            g.drawLine(0, y * t, largeurCarte, y * t);
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

    // --- Zoom API ---
    public void zoomIn() {
        setTailleCellule(tailleCellule + 4);
    }

    public void zoomOut() {
        setTailleCellule(tailleCellule - 4);
    }

    public void resetZoom() {
        setTailleCellule(Constantes.TAILLE_CELLULE_PX);
    }

    private void setTailleCellule(int nouvelle) {
        int t = Math.max(TAILLE_MIN, Math.min(TAILLE_MAX, nouvelle));
        if (t == this.tailleCellule) return;
        this.tailleCellule = t;
        if (grille != null) {
            garantirVueRemplie();
            repaint();
        }
    }

    private void defilerSelonSouris() {
        if (positionSouris == null || grille == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        int dx = 0;
        int dy = 0;

        if (positionSouris.x < MARGE_DEFILEMENT) dx = -1;
        else if (positionSouris.x > getWidth() - MARGE_DEFILEMENT) dx = 1;

        if (positionSouris.y < MARGE_DEFILEMENT) dy = -1;
        else if (positionSouris.y > getHeight() - MARGE_DEFILEMENT) dy = 1;

        if (dx != 0 || dy != 0) {
            deplacerCamera(dx, dy);
        }
    }

    private void deplacerCamera(int dx, int dy) {
        if (dx < 0 && offsetX == 0) {
            int ancienneLargeur = grille.getLargeur();
            grille.agrandir(AGRANDISSEMENT_PAR_BORD, 0, 0, 0);
            offsetX += grille.getLargeur() - ancienneLargeur;
        }
        if (dy < 0 && offsetY == 0) {
            int ancienneHauteur = grille.getHauteur();
            grille.agrandir(0, AGRANDISSEMENT_PAR_BORD, 0, 0);
            offsetY += grille.getHauteur() - ancienneHauteur;
        }

        offsetX += dx;
        offsetY += dy;

        garantirVueRemplie();
        bornerCamera();
        repaint();
    }

    private void garantirVueRemplie() {
        if (grille == null) return;

        int colonnesVisibles = getColonnesVisibles();
        int lignesVisibles = getLignesVisibles();
        int manqueDroite = offsetX + colonnesVisibles - grille.getLargeur();
        int manqueBas = offsetY + lignesVisibles - grille.getHauteur();

        grille.agrandir(
            0,
            0,
            manqueDroite > 0 ? manqueDroite + AGRANDISSEMENT_PAR_BORD : 0,
            manqueBas > 0 ? manqueBas + AGRANDISSEMENT_PAR_BORD : 0
        );
    }

    private void garantirCelluleVisible(int x, int y) {
        if (x < 0) {
            grille.agrandir(-x + AGRANDISSEMENT_PAR_BORD, 0, 0, 0);
            offsetX += -x + AGRANDISSEMENT_PAR_BORD;
        }
        if (y < 0) {
            grille.agrandir(0, -y + AGRANDISSEMENT_PAR_BORD, 0, 0);
            offsetY += -y + AGRANDISSEMENT_PAR_BORD;
        }
        garantirVueRemplie();
    }

    private void bornerCamera() {
        if (grille == null) return;
        int maxOffsetX = Math.max(0, grille.getLargeur() - getColonnesVisibles());
        int maxOffsetY = Math.max(0, grille.getHauteur() - getLignesVisibles());
        offsetX = Math.max(0, Math.min(offsetX, maxOffsetX));
        offsetY = Math.max(0, Math.min(offsetY, maxOffsetY));
    }

    private int getColonnesVisibles() {
        int t = getTailleCelluleRendue();
        return Math.max(1, (int) Math.ceil((double) getWidth() / t));
    }

    private int getLignesVisibles() {
        int t = getTailleCelluleRendue();
        return Math.max(1, (int) Math.ceil((double) getHeight() / t));
    }

    private int getTailleCelluleRendue() {
        if (grille == null || getWidth() <= 0 || getHeight() <= 0) {
            return Math.max(1, tailleCellule);
        }
        int tailleMinPourRemplirEcran = 1;
        if (grille.getLargeur() >= Grille.LARGEUR_MAX) {
            tailleMinPourRemplirEcran = Math.max(
                tailleMinPourRemplirEcran,
                (int) Math.ceil((double) getWidth() / grille.getLargeur())
            );
        }
        if (grille.getHauteur() >= Grille.HAUTEUR_MAX) {
            tailleMinPourRemplirEcran = Math.max(
                tailleMinPourRemplirEcran,
                (int) Math.ceil((double) getHeight() / grille.getHauteur())
            );
        }

        return Math.max(Math.max(1, tailleCellule), tailleMinPourRemplirEcran);
    }
}
