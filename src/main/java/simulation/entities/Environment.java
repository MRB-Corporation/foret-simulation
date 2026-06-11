package simulation.entities;

/**
 * Represents the global environmental conditions of the simulation.
 * It encapsulates the season and sunlight (shadow), applying modifiers
 * to the cells in the grid.
 */
public class Environment {

    public enum Season {
        DRY("Dry (-20% humidity)", -0.2),
        NORMAL("Normal", 0.0),
        WET("Wet (+30% humidity)", 0.3);

        private final String displayName;
        private final double humidityModifier;

        Season(String displayName, double humidityModifier) {
            this.displayName = displayName;
            this.humidityModifier = humidityModifier;
        }

        public String getDisplayName() { return displayName; }
        public double getHumidityModifier() { return humidityModifier; }
    }

    public enum Sunlight {
        SHADOW("Shadow (-10°C)", -10.0),
        NORMAL("Normal", 0.0),
        SUNNY("Sunny (+15°C)", 15.0);

        private final String displayName;
        private final double tempModifier;

        Sunlight(String displayName, double tempModifier) {
            this.displayName = displayName;
            this.tempModifier = tempModifier;
        }

        public String getDisplayName() { return displayName; }
        public double getTempModifier() { return tempModifier; }
    }

    private Season season = Season.NORMAL;
    private Sunlight sunlight = Sunlight.NORMAL;

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public Sunlight getSunlight() { return sunlight; }
    public void setSunlight(Sunlight sunlight) { this.sunlight = sunlight; }

    /**
     * Applies the current environmental modifiers to all cells in the grid.
     * This alters the base temperature and humidity of every cell.
     * 
     * @param grid the grid to apply the environment to
     */
    public void applyTo(Grid grid) {
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Cell c = grid.getCell(x, y);
                
                // Adjust humidity based on season
                c.adjustHumidity(season.getHumidityModifier());
                
                // Adjust temperature based on sunlight
                c.heat(sunlight.getTempModifier());
            }
        }
    }
}
