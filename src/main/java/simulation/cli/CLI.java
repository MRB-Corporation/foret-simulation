package simulation.cli;

import simulation.algorithme.MooreSpread;
import simulation.algorithme.OrthogonalSpread;
import simulation.algorithme.RadialSpread;
import simulation.algorithme.SpreadAlgorithm;
import simulation.chargeur.GridLoader;
import simulation.modele.Cell;
import simulation.modele.CellState;
import simulation.modele.Grid;
import simulation.modele.Wind;
import simulation.stats.GridStats;
import simulation.utilitaire.PhysicsFormulas;

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

        // Auto-ignite centre cell
        Cell centre = grid.getCell(width / 2, height / 2);
        if (centre != null && centre.canIgnite()) {
            centre.setState(CellState.ON_FIRE);
            centre.setBurnCounter(PhysicsFormulas.burnDuration(centre));
        }

        System.out.printf("%nStarting simulation: %dx%d, %s, toroidal=%b%n",
                width, height, algorithm.getName(), toroidal);
        System.out.println("─".repeat(width + 4));

        // Run
        for (int step = 1; step <= maxSteps; step++) {
            grid = timeStep(grid, algorithm, wind);
            GridStats stats = GridStats.compute(grid, step);

            System.out.printf("Step %3d │ Burning: %3d │ Burned: %3d │ Intact: %3d%n",
                    step, stats.getBurningCells(), stats.getBurnedCells(), stats.getIntactCells());
            printGrid(grid);

            if (stats.getBurningCells() == 0) {
                System.out.println("Fire extinguished at step " + step + ".");
                break;
            }
        }

        System.out.println("Simulation complete.");
        sc.close();
    }

    /**
     * Performs one time step on the given grid and returns the updated grid.
     * Mirrors the logic in {@link simulation.Simulator#timeStep()}.
     *
     * @param grid      current grid
     * @param algorithm spread algorithm
     * @param wind      current wind
     * @return updated grid (double-buffered copy)
     */
    private static Grid timeStep(Grid grid, SpreadAlgorithm algorithm, Wind wind) {
        Grid next = copyGrid(grid);

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell current = grid.getCell(x, y);
                if (current.getState() != CellState.ON_FIRE) continue;

                Cell currentNext = next.getCell(x, y);

                for (Cell nb : algorithm.getNeighbours(current, grid)) {
                    Cell nbNext = next.getCell(nb.getX(), nb.getY());
                    nbNext.heat(PhysicsFormulas.temperatureDelta(nb));
                    nbNext.adjustHumidity(-0.02);
                }

                for (Cell candidate : algorithm.getCandidates(current, grid)) {
                    int dx = candidate.getX() - current.getX();
                    int dy = candidate.getY() - current.getY();
                    Cell candidateNext = next.getCell(candidate.getX(), candidate.getY());
                    double prob = PhysicsFormulas.ignitionProbabilityWithWind(candidate, wind, dx, dy);
                    if (RNG.nextDouble() < prob && candidate.canIgnite()) {
                        candidateNext.setState(CellState.ON_FIRE);
                        candidateNext.setBurnCounter(PhysicsFormulas.burnDuration(candidate));
                    }
                }

                currentNext.decrementBurnCounter();
                if (currentNext.getBurnCounter() <= 0) currentNext.setState(CellState.BURNED);
            }
        }
        return next;
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

    private static Grid copyGrid(Grid source) {
        Grid copy = new Grid(source.getWidth(), source.getHeight(), source.isToroidal());
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Cell src = source.getCell(x, y);
                Cell dst = copy.getCell(x, y);
                dst.setState(src.getState());
                dst.setTerrain(src.getTerrain());
                dst.setHumidity(src.getHumidity());
                dst.setInflammability(src.getInflammability());
                dst.setResistance(src.getResistance());
                dst.setVegetationDensity(src.getVegetationDensity());
                dst.setTemperature(src.getTemperature());
                dst.setBurnCounter(src.getBurnCounter());
            }
        }
        return copy;
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
