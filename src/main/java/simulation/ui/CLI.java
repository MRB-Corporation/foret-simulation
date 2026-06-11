package simulation.ui;

import simulation.services.MooreSpread;
import simulation.services.OrthogonalSpread;
import simulation.services.RadialSpread;
import simulation.services.SpreadAlgorithm;
import simulation.dao.GridLoader;
import simulation.entities.Cell;
import simulation.entities.CellState;
import simulation.entities.Grid;
import simulation.entities.Wind;
import simulation.entities.GridStats;
import simulation.entities.Environment;
import simulation.services.Simulator;
import simulation.utils.PhysicsFormulas;

import java.util.Random;
import java.util.Scanner;

/**
 * Command-line interface for the forest-fire simulation.
 *
 * <p>Allows testing the simulation model without the graphical interface.
 * The grid is printed as ASCII art each step:</p>
 * <pre>
 *   . = intact forest
 *   F = on fire
 *   X = burned
 *   ~ = water / wet zone
 *   # = firebreak / urban
 * </pre>
 *
 * <p>Launch with: {@code java -cp bin simulation.cli.CLI}</p>
 */
public class CLI {

    private static final Random RNG = new Random();

    /** Entry point for command-line mode. */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Forest Fire Simulation — CLI mode ===");
        System.out.println("Grid width  [default 20]: ");
        int width  = readInt(sc, 20);
        System.out.println("Grid height [default 20]: ");
        int height = readInt(sc, 20);

        System.out.println("Algorithm: 1=Orthogonal  2=Moore  3=Radial [default 1]: ");
        int algoChoice = readInt(sc, 1);
        SpreadAlgorithm algorithm = switch (algoChoice) {
            case 2  -> new MooreSpread();
            case 3  -> new RadialSpread();
            default -> new OrthogonalSpread();
        };

        System.out.println("Toroidal grid? (y/n) [default n]: ");
        boolean toroidal = "y".equalsIgnoreCase(sc.nextLine().trim());

        System.out.println("Steps to run [default 30]: ");
        int maxSteps = readInt(sc, 30);

        // Build grid
        Grid grid = GridLoader.generateRandom(width, height);
        grid.setToroidal(toroidal);
        Wind wind = new Wind(0.0, 0.0);

        Simulator simulator = new Simulator();
        simulator.loadGrid(grid);
        simulator.setWind(wind);
        simulator.setAlgorithm(algorithm);

        // Auto-ignite centre cell
        simulator.igniteCell(width / 2, height / 2);

        System.out.printf("%nStarting simulation: %dx%d, %s, toroidal=%b%n",
                width, height, algorithm.getClass().getSimpleName(), toroidal);
        System.out.println("─".repeat(width + 4));

        // Run
        for (int step = 1; step <= maxSteps; step++) {
            simulator.timeStep();
            Grid currentGrid = simulator.getGrid();
            GridStats stats = GridStats.compute(currentGrid, simulator.getStep());

            System.out.printf("Step %3d │ Burning: %3d │ Burned: %3d │ Intact: %3d%n",
                    simulator.getStep(), stats.getBurningCells(), stats.getBurnedCells(), stats.getIntactCells());
            printGrid(currentGrid);

            if (stats.getBurningCells() == 0) {
                System.out.println("Fire extinguished at step " + simulator.getStep() + ".");
                break;
            }
        }

        System.out.println("Simulation complete.");
        sc.close();
    }

    /**
     * Prints the grid as ASCII art to standard output.
     *
     * @param grid grid to display
     */
    private static void printGrid(Grid grid) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < grid.getHeight(); y++) {
            sb.append("  ");
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell c = grid.getCell(x, y);
                sb.append(switch (c.getState()) {
                    case ON_FIRE -> 'F';
                    case BURNED  -> 'X';
                    default -> switch (c.getTerrain()) {
                        case WATER, WET_ZONE   -> '~';
                        case FIREBREAK, URBAN_ZONE -> '#';
                        default -> '.';
                    };
                });
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }

    private static int readInt(Scanner sc, int defaultValue) {
        try {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return defaultValue;
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
