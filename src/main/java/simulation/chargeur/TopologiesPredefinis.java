package simulation.chargeur;

import simulation.modele.Cellule;
import simulation.modele.Grille;
import simulation.modele.TypeTerrain;

import java.util.Random;

public final class TopologiesPredefinis {

    private TopologiesPredefinis() {}

    // ── 1. Forêt dense ────────────────────────────────────────────────────────
    // Végétation très dense, humidité basse : feu rapide et dévastateur.
    // Quelques taches de zones humides servent de refuges naturels.
    public static Grille foretDense() {
        int w = Grille.LARGEUR_DEFAUT, h = Grille.HAUTEUR_DEFAUT;
        Grille g = new Grille(w, h);
        Random rng = new Random(42);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cellule c = g.getCellule(x, y);
                c.setTerrain(TypeTerrain.FORET);
                c.setDensiteVegetation(0.75 + rng.nextDouble() * 0.25);
                c.setHumidite(0.05 + rng.nextDouble() * 0.15);
                c.setInflammabilite(0.65 + rng.nextDouble() * 0.25);
                c.setResistance(0.3 + rng.nextDouble() * 0.3);
            }
        }

        // Taches de zones humides (refuges circulaires)
        int[][] centres = {{10,8},{15,35},{38,12},{25,42},{5,25},{42,30},{30,20}};
        for (int[] p : centres) {
            int rayon = 2 + rng.nextInt(2);
            for (int dy = -rayon; dy <= rayon; dy++) {
                for (int dx = -rayon; dx <= rayon; dx++) {
                    if (dx * dx + dy * dy <= rayon * rayon) {
                        Cellule c = g.getCellule(p[0] + dx, p[1] + dy);
                        if (c != null) {
                            c.setTerrain(TypeTerrain.ZONE_HUMIDE);
                            c.setHumidite(0.70 + rng.nextDouble() * 0.25);
                            c.setInflammabilite(0.05 + rng.nextDouble() * 0.05);
                        }
                    }
                }
            }
        }
        return g;
    }

    // ── 2. Rivière en forêt ───────────────────────────────────────────────────
    // Une rivière diagonale traverse la forêt avec des berges humides.
    // Le feu se propage librement de chaque côté mais est bloqué par l'eau.
    public static Grille riviereEnForet() {
        int w = Grille.LARGEUR_DEFAUT, h = Grille.HAUTEUR_DEFAUT;
        Grille g = new Grille(w, h);
        Random rng = new Random(7);

        // Forêt de base avec variété
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cellule c = g.getCellule(x, y);
                c.setTerrain(TypeTerrain.FORET);
                c.setDensiteVegetation(0.4 + rng.nextDouble() * 0.5);
                c.setHumidite(0.1 + rng.nextDouble() * 0.25);
                c.setInflammabilite(0.45 + rng.nextDouble() * 0.35);
                c.setResistance(0.2 + rng.nextDouble() * 0.3);
            }
        }

        // Rivière diagonale : y=15 (x=0) → y=35 (x=49)
        for (int x = 0; x < w; x++) {
            int cy = 15 + x * 20 / (w - 1);

            // Coeur de la rivière (3 cellules de large)
            for (int dy = -1; dy <= 1; dy++) {
                Cellule c = g.getCellule(x, cy + dy);
                if (c != null) {
                    c.setTerrain(TypeTerrain.EAU);
                    c.setHumidite(1.0);
                    c.setInflammabilite(0.0);
                }
            }
            // Berges humides (2 cellules de chaque côté)
            for (int dy : new int[]{-3, -2, 2, 3}) {
                Cellule c = g.getCellule(x, cy + dy);
                if (c != null && c.getTerrain() == TypeTerrain.FORET) {
                    c.setTerrain(TypeTerrain.ZONE_HUMIDE);
                    c.setHumidite(0.70 + rng.nextDouble() * 0.2);
                    c.setInflammabilite(0.05 + rng.nextDouble() * 0.05);
                }
            }
        }
        return g;
    }

    // ── 3. Ville avec cour centrale ───────────────────────────────────────────
    // Des immeubles (ZONE_URBANISEE) entourent une cour intérieure.
    // La cour contient un parc (FORET) avec une fontaine (EAU) au centre.
    // Les rues (COUPE_FEU) séparent les blocs de bâtiments.
    //
    // Découpage de la grille 50x50 (routes aux colonnes/lignes 5,16,33,44) :
    //   [ext][route][bloc][route][  parc  ][route][bloc][route][ext]
    //    0-4   5    6-15   16   17-32      33   34-43   44   45-49
    public static Grille villeAvecCour() {
        int w = Grille.LARGEUR_DEFAUT, h = Grille.HAUTEUR_DEFAUT;
        Grille g = new Grille(w, h);

        // Tout en bâtiments par défaut
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cellule c = g.getCellule(x, y);
                c.setTerrain(TypeTerrain.ZONE_URBANISEE);
                c.setInflammabilite(0.0);
            }
        }

        // Rues (coupe-feux) formant la grille urbaine
        int[] routes = {5, 16, 33, 44};
        for (int r : routes) {
            for (int i = 0; i < w; i++) {
                appliquerRoute(g, i, r); // horizontale
                appliquerRoute(g, r, i); // verticale
            }
        }

        // Parc central : x=[17,32], y=[17,32]
        Random rng = new Random(13);
        for (int y = 17; y <= 32; y++) {
            for (int x = 17; x <= 32; x++) {
                Cellule c = g.getCellule(x, y);
                c.setTerrain(TypeTerrain.FORET);
                c.setDensiteVegetation(0.60 + rng.nextDouble() * 0.30);
                c.setHumidite(0.30 + rng.nextDouble() * 0.15);
                c.setInflammabilite(0.40 + rng.nextDouble() * 0.20);
                c.setResistance(0.25 + rng.nextDouble() * 0.20);
            }
        }

        // Fontaine centrale : x=[22,27], y=[22,27]
        for (int y = 22; y <= 27; y++) {
            for (int x = 22; x <= 27; x++) {
                Cellule c = g.getCellule(x, y);
                c.setTerrain(TypeTerrain.EAU);
                c.setHumidite(1.0);
                c.setInflammabilite(0.0);
            }
        }

        return g;
    }

    private static void appliquerRoute(Grille g, int x, int y) {
        Cellule c = g.getCellule(x, y);
        if (c != null) {
            c.setTerrain(TypeTerrain.COUPE_FEU);
            c.setInflammabilite(0.0);
        }
    }
}
