package simulation.chargeur;

import simulation.modele.Cellule;
import simulation.modele.EtatCellule;
import simulation.modele.Grille;

import java.io.*;
import java.util.Random;

public class ChargeurTopologie {

    private static final Random random = new Random();

    // ============================================================
    // GENERATION ALEATOIRE
    // ============================================================

    public static Grille genererAleatoire(int largeur, int hauteur) {
        Grille grille = new Grille(largeur, hauteur);

        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                Cellule cellule = grille.getCellule(x, y);
                appliquerEtatAleatoire(cellule);
            }
        }

        return grille;
    }

    // Constructeur par défaut avec taille par défaut
    public static Grille genererAleatoire() {
        return genererAleatoire(Grille.LARGEUR_DEFAUT, Grille.HAUTEUR_DEFAUT);
    }

    private static void appliquerEtatAleatoire(Cellule cellule) {
        double tirage = random.nextDouble(); // entre 0.0 et 1.0

        // Probabilités de chaque type de terrain
        if (tirage < 0.05) {
            cellule.setEtat(EtatCellule.EAU);
            cellule.setHumidite(1.0);
            cellule.setInflammabilite(0.0);
        } else if (tirage < 0.10) {
            cellule.setEtat(EtatCellule.COUPE_FEU);
            cellule.setInflammabilite(0.0);
        } else if (tirage < 0.15) {
            cellule.setEtat(EtatCellule.HUMIDE);
            cellule.setHumidite(0.8);
            cellule.setInflammabilite(0.1);
        } else if (tirage < 0.18) {
            cellule.setEtat(EtatCellule.ZONE_URBANISEE);
            cellule.setInflammabilite(0.0);
        } else {
            // Forêt normale (majoritaire)
            cellule.setEtat(EtatCellule.INTACT);
            cellule.setHumidite(0.2 + random.nextDouble() * 0.4);
            cellule.setInflammabilite(0.4 + random.nextDouble() * 0.4);
            cellule.setResistance(0.2 + random.nextDouble() * 0.3);
            cellule.setDensiteVegetation(0.5 + random.nextDouble() * 0.5);
        }
    }

    // ============================================================
    // SAUVEGARDE
    // ============================================================

    public static void sauvegarder(Grille grille, String chemin) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chemin))) {

            // Première ligne : dimensions
            writer.write(grille.getLargeur() + "," + grille.getHauteur());
            writer.newLine();

            // Une ligne par cellule : x,y,etat,humidite,inflammabilite,resistance,densite,temperature
            for (int y = 0; y < grille.getHauteur(); y++) {
                for (int x = 0; x < grille.getLargeur(); x++) {
                    Cellule c = grille.getCellule(x, y);
                    writer.write(
                        x + "," + y + "," +
                        c.getEtat().name() + "," +
                        c.getHumidite() + "," +
                        c.getInflammabilite() + "," +
                        c.getResistance() + "," +
                        c.getDensiteVegetation() + "," +
                        c.getTemperature()
                    );
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    // ============================================================
    // CHARGEMENT DEPUIS FICHIER
    // ============================================================

    public static Grille chargerFichier(String chemin) {
        try (BufferedReader reader = new BufferedReader(new FileReader(chemin))) {

            // Première ligne : dimensions
            String[] dimensions = reader.readLine().split(",");
            int largeur = Integer.parseInt(dimensions[0]);
            int hauteur = Integer.parseInt(dimensions[1]);

            Grille grille = new Grille(largeur, hauteur);

            // Lecture cellule par cellule
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                String[] parts = ligne.split(",");

                int x                = Integer.parseInt(parts[0]);
                int y                = Integer.parseInt(parts[1]);
                EtatCellule etat     = EtatCellule.valueOf(parts[2]);
                double humidite      = Double.parseDouble(parts[3]);
                double inflammabilite = Double.parseDouble(parts[4]);
                double resistance    = Double.parseDouble(parts[5]);
                double densite       = Double.parseDouble(parts[6]);
                double temperature   = Double.parseDouble(parts[7]);

                Cellule c = grille.getCellule(x, y);
                c.setEtat(etat);
                c.setHumidite(humidite);
                c.setInflammabilite(inflammabilite);
                c.setResistance(resistance);
                c.setDensiteVegetation(densite);
                c.setTemperature(temperature);
            }

            return grille;

        } catch (IOException e) {
            System.err.println("Erreur chargement : " + e.getMessage());
            return new Grille(); // grille par défaut si erreur
        }
    }
}

// TODO
