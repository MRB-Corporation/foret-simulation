package simulation;

import config.SimulationConfig;
import model.Cell;
import model.CellState;
import model.Grid;
import model.TerrainType;

import java.io.*;

/**
 * Gère la sauvegarde et le chargement de la grille dans un fichier CSV.
 *
 * Format du fichier :
 *   ligne 1 : largeur,hauteur
 *   lignes suivantes : x,y,état,terrain,humidité,température
 */
public class GridFileManager {

    /** Constructeur privé : classe utilitaire, ne pas instancier. */
    private GridFileManager() {}

    /**
     * Sauvegarde la grille dans un fichier CSV.
     *
     * @param grid la grille à sauvegarder
     * @param path chemin du fichier
     * @throws IOException si l'écriture échoue
     */
    public static void save(Grid grid, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {

            // Première ligne : les dimensions
            writer.write(grid.getWidth() + "," + grid.getHeight());
            writer.newLine();

            // Une ligne par cellule
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int x = 0; x < grid.getWidth(); x++) {
                    Cell c = grid.getCell(x, y);
                    writer.write(
                        x + "," + y + ","
                        + c.getState().name() + ","
                        + c.getTerrain().name() + ","
                        + c.getHumidity() + ","
                        + c.getTemperature()
                    );
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Charge une grille depuis un fichier CSV.
     *
     * @param path   chemin du fichier
     * @param config configuration (les dimensions seront mises à jour)
     * @return la grille chargée
     * @throws IOException si la lecture échoue ou si le format est invalide
     */
    public static Grid load(String path, SimulationConfig config) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            // Lire les dimensions
            String[] dims = reader.readLine().split(",");
            config.gridWidth  = Integer.parseInt(dims[0]);
            config.gridHeight = Integer.parseInt(dims[1]);

            Grid grid = new Grid(config);

            // Lire chaque cellule
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);

                Cell c = grid.getCell(x, y);
                c.setState(CellState.valueOf(parts[2]));
                c.setTerrain(TerrainType.valueOf(parts[3]));
                c.setHumidity(Double.parseDouble(parts[4]));
                c.setTemperature(Double.parseDouble(parts[5]));
            }

            return grid;
        }
    }
}