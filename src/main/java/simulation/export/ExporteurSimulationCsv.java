package simulation.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ExporteurSimulationCsv {

    private static final String SEPARATEUR = ";";

    private ExporteurSimulationCsv() {
    }

    public static void exporter(List<InstantaneSimulation> historique, String chemin) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chemin))) {
            writer.write(String.join(SEPARATEUR,
                "pas",
                "largeur",
                "hauteur",
                "total_cellules",
                "cellules_intactes",
                "cellules_en_feu",
                "cellules_brulees",
                "terrains_foret",
                "terrains_zone_humide",
                "terrains_coupe_feu",
                "terrains_eau",
                "terrains_zone_urbanisee",
                "temperature_moyenne",
                "humidite_moyenne",
                "inflammabilite_moyenne",
                "resistance_moyenne",
                "densite_vegetation_moyenne",
                "algorithme",
                "vent_direction_radians",
                "vent_vitesse"
            ));
            writer.newLine();

            for (InstantaneSimulation instantane : historique) {
                writer.write(ligne(instantane));
                writer.newLine();
            }
        }
    }

    private static String ligne(InstantaneSimulation instantane) {
        return String.join(SEPARATEUR,
            entier(instantane.getPas()),
            entier(instantane.getLargeur()),
            entier(instantane.getHauteur()),
            entier(instantane.getTotalCellules()),
            entier(instantane.getCellulesIntactes()),
            entier(instantane.getCellulesEnFeu()),
            entier(instantane.getCellulesBrulees()),
            entier(instantane.getTerrainsForet()),
            entier(instantane.getTerrainsHumides()),
            entier(instantane.getTerrainsCoupeFeu()),
            entier(instantane.getTerrainsEau()),
            entier(instantane.getTerrainsUrbanises()),
            decimal(instantane.getTemperatureMoyenne()),
            decimal(instantane.getHumiditeMoyenne()),
            decimal(instantane.getInflammabiliteMoyenne()),
            decimal(instantane.getResistanceMoyenne()),
            decimal(instantane.getDensiteVegetationMoyenne()),
            texte(instantane.getAlgorithme()),
            decimal(instantane.getVentDirection()),
            decimal(instantane.getVentVitesse())
        );
    }

    private static String entier(int valeur) {
        return Integer.toString(valeur);
    }

    private static String decimal(double valeur) {
        return String.format(Locale.US, "%.6f", valeur);
    }

    private static String texte(String valeur) {
        return "\"" + valeur.replace("\"", "\"\"") + "\"";
    }
}
