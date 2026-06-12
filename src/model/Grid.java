package model;

import config.SimulationConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente la grille 2D de la simulation.
 * Gère l'accès aux cellules et le calcul des voisins,
 * avec support de la topologie torique (bords qui se rejoignent).
 */
public class Grid {

    /** Tableau 2D de cellules : cells[ligne][colonne]. */
    private final Cell[][] cells;

    /** Nombre de colonnes. */
    private final int width;

    /** Nombre de lignes. */
    private final int height;

    /**
     * Crée une grille et initialise toutes les cellules aléatoirement.
     *
     * @param config configuration de la simulation
     */
    public Grid(SimulationConfig config) {
        this.width  = config.gridWidth;
        this.height = config.gridHeight;
        this.cells  = new Cell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell(x, y, config);
            }
        }
    }

    /**
     * Retourne la cellule à la position (x, y).
     *
     * @param x colonne
     * @param y ligne
     * @return la cellule, ou null si hors grille
     */
    public Cell getCell(int x, int y) {
        if (isValid(x, y)) {
            return cells[y][x];
        }
        return null;
    }

    /**
     * Vérifie si les coordonnées sont dans la grille.
     *
     * @param x colonne
     * @param y ligne
     * @return true si les coordonnées sont valides
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Retourne la liste des voisins d'une cellule.
     * Le comportement dépend de la configuration :
     * - neighborRadius : taille du voisinage
     * - useDiagonals : inclure ou non les diagonales
     * - toricTopology : si true, les bords se rejoignent
     *
     * @param cell   la cellule centrale
     * @param config configuration de la simulation
     * @return liste des cellules voisines
     */
    public List<Cell> getNeighbors(Cell cell, SimulationConfig config) {
        List<Cell> neighbors = new ArrayList<>();
        int cx = cell.getX();
        int cy = cell.getY();
        int radius = config.neighborRadius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {

                // On ignore la cellule elle-même
                if (dx == 0 && dy == 0) continue;

                // Sans diagonales : on ignore les déplacements en diagonale
                if (!config.useDiagonals && dx != 0 && dy != 0) continue;

                int nx = cx + dx;
                int ny = cy + dy;

                if (config.toricTopology) {
                    // Topologie torique : on "enroule" les coordonnées
                    // avec un modulo. Le +width évite les nombres négatifs.
                    nx = ((nx % width) + width) % width;
                    ny = ((ny % height) + height) % height;
                    neighbors.add(cells[ny][nx]);
                } else {
                    // Topologie bornée : on ignore les cases hors grille
                    if (isValid(nx, ny)) {
                        neighbors.add(cells[ny][nx]);
                    }
                }
            }
        }

        return neighbors;
    }

    /** @return le nombre de colonnes */
    public int getWidth() { return width; }

    /** @return le nombre de lignes */
    public int getHeight() { return height; }
}