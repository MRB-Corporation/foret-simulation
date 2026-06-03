package simulation.modele;

import java.util.Random;

public class Grille {

    // 1. Attributs
    private int largeur;
    private int hauteur;
    private Cellule[][] cellules;
    private static final Random RANDOM = new Random();
    public static final int LARGEUR_MAX = 500;
    public static final int HAUTEUR_MAX = 500;

    public static final int LARGEUR_DEFAUT = 50;
    public static final int HAUTEUR_DEFAUT = 50;

    // 2. Constructeurs
    public Grille(int largeur, int hauteur) {
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.cellules = new Cellule[hauteur][largeur];
        initialiser();
    }

    public Grille() {
        this(LARGEUR_DEFAUT, HAUTEUR_DEFAUT);
    }

    // 3. Méthodes métier
    private void initialiser() {
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                cellules[y][x] = creerCelluleAleatoire(x, y);
            }
        }
    }

    public Cellule getCellule(int x, int y) {
        if (estValide(x, y)) {
            return cellules[y][x];
        }
        return null;
    }

    public void setCellule(int x, int y, Cellule c) {
        if (estValide(x, y)) {
            cellules[y][x] = c;
        }
    }

    public boolean estValide(int x, int y) {
        return x >= 0 && x < largeur && y >= 0 && y < hauteur;
    }

    public void agrandir(int gauche, int haut, int droite, int bas) {
        gauche = Math.max(0, gauche);
        haut = Math.max(0, haut);
        droite = Math.max(0, droite);
        bas = Math.max(0, bas);

        int largeurAjoutable = Math.max(0, LARGEUR_MAX - largeur);
        int hauteurAjoutable = Math.max(0, HAUTEUR_MAX - hauteur);

        if (gauche + droite > largeurAjoutable) {
            int gaucheDemandee = gauche;
            gauche = Math.min(gauche, largeurAjoutable);
            droite = Math.min(droite, largeurAjoutable - gauche);

            int reste = largeurAjoutable - gauche - droite;
            if (reste > 0 && gaucheDemandee > gauche) {
                gauche += Math.min(reste, gaucheDemandee - gauche);
            }
        }

        if (haut + bas > hauteurAjoutable) {
            int hautDemande = haut;
            haut = Math.min(haut, hauteurAjoutable);
            bas = Math.min(bas, hauteurAjoutable - haut);

            int reste = hauteurAjoutable - haut - bas;
            if (reste > 0 && hautDemande > haut) {
                haut += Math.min(reste, hautDemande - haut);
            }
        }

        if (gauche == 0 && haut == 0 && droite == 0 && bas == 0) {
            return;
        }

        int nouvelleLargeur = largeur + gauche + droite;
        int nouvelleHauteur = hauteur + haut + bas;
        Cellule[][] nouvellesCellules = new Cellule[nouvelleHauteur][nouvelleLargeur];

        for (int y = 0; y < nouvelleHauteur; y++) {
            for (int x = 0; x < nouvelleLargeur; x++) {
                int ancienX = x - gauche;
                int ancienY = y - haut;

                if (estValide(ancienX, ancienY)) {
                    nouvellesCellules[y][x] = copierCellule(cellules[ancienY][ancienX], x, y);
                } else {
                    nouvellesCellules[y][x] = creerCelluleAleatoire(x, y);
                }
            }
        }

        largeur = nouvelleLargeur;
        hauteur = nouvelleHauteur;
        cellules = nouvellesCellules;
    }

    private Cellule copierCellule(Cellule source, int x, int y) {
        Cellule copie = new Cellule(x, y);
        copie.setEtat(source.getEtat());
        copie.setTerrain(source.getTerrain());
        copie.setHumidite(source.getHumidite());
        copie.setInflammabilite(source.getInflammabilite());
        copie.setResistance(source.getResistance());
        copie.setDensiteVegetation(source.getDensiteVegetation());
        copie.setTemperature(source.getTemperature());
        copie.setCompteurCombuston(source.getCompteurCombuston());
        return copie;
    }

    private Cellule creerCelluleAleatoire(int x, int y) {
        Cellule cellule = new Cellule(x, y);
        double tirage = RANDOM.nextDouble();

        if (tirage < 0.05) {
            cellule.setTerrain(TypeTerrain.EAU);
            cellule.setHumidite(1.0);
            cellule.setInflammabilite(0.0);
        } else if (tirage < 0.10) {
            cellule.setTerrain(TypeTerrain.COUPE_FEU);
            cellule.setInflammabilite(0.0);
        } else if (tirage < 0.15) {
            cellule.setTerrain(TypeTerrain.ZONE_HUMIDE);
            cellule.setHumidite(0.8);
            cellule.setInflammabilite(0.1);
        } else if (tirage < 0.18) {
            cellule.setTerrain(TypeTerrain.ZONE_URBANISEE);
            cellule.setInflammabilite(0.0);
        } else {
            cellule.setTerrain(TypeTerrain.FORET);
            cellule.setHumidite(0.2 + RANDOM.nextDouble() * 0.4);
            cellule.setInflammabilite(0.4 + RANDOM.nextDouble() * 0.4);
            cellule.setResistance(0.2 + RANDOM.nextDouble() * 0.3);
            cellule.setDensiteVegetation(0.5 + RANDOM.nextDouble() * 0.5);
        }

        return cellule;
    }

    // 4. Getters / Setters
    public int getLargeur() { return largeur; }
    public int getHauteur() { return hauteur; }
}

// TODO
