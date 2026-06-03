package simulation.export;

import simulation.modele.Cellule;
import simulation.modele.EtatCellule;
import simulation.modele.Grille;
import simulation.modele.TypeTerrain;
import simulation.modele.Vent;

public class InstantaneSimulation {

    private final int pas;
    private final int largeur;
    private final int hauteur;
    private final int totalCellules;
    private final int cellulesIntactes;
    private final int cellulesEnFeu;
    private final int cellulesBrulees;
    private final int terrainsForet;
    private final int terrainsHumides;
    private final int terrainsCoupeFeu;
    private final int terrainsEau;
    private final int terrainsUrbanises;
    private final double temperatureMoyenne;
    private final double humiditeMoyenne;
    private final double inflammabiliteMoyenne;
    private final double resistanceMoyenne;
    private final double densiteVegetationMoyenne;
    private final String algorithme;
    private final double ventDirection;
    private final double ventVitesse;

    private InstantaneSimulation(
        int pas,
        int largeur,
        int hauteur,
        int totalCellules,
        int cellulesIntactes,
        int cellulesEnFeu,
        int cellulesBrulees,
        int terrainsForet,
        int terrainsHumides,
        int terrainsCoupeFeu,
        int terrainsEau,
        int terrainsUrbanises,
        double temperatureMoyenne,
        double humiditeMoyenne,
        double inflammabiliteMoyenne,
        double resistanceMoyenne,
        double densiteVegetationMoyenne,
        String algorithme,
        double ventDirection,
        double ventVitesse
    ) {
        this.pas = pas;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.totalCellules = totalCellules;
        this.cellulesIntactes = cellulesIntactes;
        this.cellulesEnFeu = cellulesEnFeu;
        this.cellulesBrulees = cellulesBrulees;
        this.terrainsForet = terrainsForet;
        this.terrainsHumides = terrainsHumides;
        this.terrainsCoupeFeu = terrainsCoupeFeu;
        this.terrainsEau = terrainsEau;
        this.terrainsUrbanises = terrainsUrbanises;
        this.temperatureMoyenne = temperatureMoyenne;
        this.humiditeMoyenne = humiditeMoyenne;
        this.inflammabiliteMoyenne = inflammabiliteMoyenne;
        this.resistanceMoyenne = resistanceMoyenne;
        this.densiteVegetationMoyenne = densiteVegetationMoyenne;
        this.algorithme = algorithme;
        this.ventDirection = ventDirection;
        this.ventVitesse = ventVitesse;
    }

    public static InstantaneSimulation depuis(Grille grille, int pas, String algorithme, Vent vent) {
        int cellulesIntactes = 0;
        int cellulesEnFeu = 0;
        int cellulesBrulees = 0;
        int terrainsForet = 0;
        int terrainsHumides = 0;
        int terrainsCoupeFeu = 0;
        int terrainsEau = 0;
        int terrainsUrbanises = 0;
        double temperature = 0.0;
        double humidite = 0.0;
        double inflammabilite = 0.0;
        double resistance = 0.0;
        double densiteVegetation = 0.0;

        for (int y = 0; y < grille.getHauteur(); y++) {
            for (int x = 0; x < grille.getLargeur(); x++) {
                Cellule cellule = grille.getCellule(x, y);

                if (cellule.getEtat() == EtatCellule.INTACT) cellulesIntactes++;
                else if (cellule.getEtat() == EtatCellule.EN_FEU) cellulesEnFeu++;
                else if (cellule.getEtat() == EtatCellule.BRULE) cellulesBrulees++;

                if (cellule.getTerrain() == TypeTerrain.FORET) terrainsForet++;
                else if (cellule.getTerrain() == TypeTerrain.ZONE_HUMIDE) terrainsHumides++;
                else if (cellule.getTerrain() == TypeTerrain.COUPE_FEU) terrainsCoupeFeu++;
                else if (cellule.getTerrain() == TypeTerrain.EAU) terrainsEau++;
                else if (cellule.getTerrain() == TypeTerrain.ZONE_URBANISEE) terrainsUrbanises++;

                temperature += cellule.getTemperature();
                humidite += cellule.getHumidite();
                inflammabilite += cellule.getInflammabilite();
                resistance += cellule.getResistance();
                densiteVegetation += cellule.getDensiteVegetation();
            }
        }

        int totalCellules = grille.getLargeur() * grille.getHauteur();
        double diviseur = Math.max(1, totalCellules);

        return new InstantaneSimulation(
            pas,
            grille.getLargeur(),
            grille.getHauteur(),
            totalCellules,
            cellulesIntactes,
            cellulesEnFeu,
            cellulesBrulees,
            terrainsForet,
            terrainsHumides,
            terrainsCoupeFeu,
            terrainsEau,
            terrainsUrbanises,
            temperature / diviseur,
            humidite / diviseur,
            inflammabilite / diviseur,
            resistance / diviseur,
            densiteVegetation / diviseur,
            algorithme,
            vent.getDirection(),
            vent.getVitesse()
        );
    }

    public int getPas() { return pas; }
    public int getLargeur() { return largeur; }
    public int getHauteur() { return hauteur; }
    public int getTotalCellules() { return totalCellules; }
    public int getCellulesIntactes() { return cellulesIntactes; }
    public int getCellulesEnFeu() { return cellulesEnFeu; }
    public int getCellulesBrulees() { return cellulesBrulees; }
    public int getTerrainsForet() { return terrainsForet; }
    public int getTerrainsHumides() { return terrainsHumides; }
    public int getTerrainsCoupeFeu() { return terrainsCoupeFeu; }
    public int getTerrainsEau() { return terrainsEau; }
    public int getTerrainsUrbanises() { return terrainsUrbanises; }
    public double getTemperatureMoyenne() { return temperatureMoyenne; }
    public double getHumiditeMoyenne() { return humiditeMoyenne; }
    public double getInflammabiliteMoyenne() { return inflammabiliteMoyenne; }
    public double getResistanceMoyenne() { return resistanceMoyenne; }
    public double getDensiteVegetationMoyenne() { return densiteVegetationMoyenne; }
    public String getAlgorithme() { return algorithme; }
    public double getVentDirection() { return ventDirection; }
    public double getVentVitesse() { return ventVitesse; }
}
