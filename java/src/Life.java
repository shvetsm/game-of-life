import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Life {
    static final Random random = new Random();

    static int width;
    static int height;
    static double speed;
    static double density;

    public static void main(String[] args) throws Exception {
        Map<String, String> env = loadEnv("../.env");

        width = getInt(env, "WIDTH", 40);
        height = getInt(env, "HEIGHT", 20);
        speed = getDouble(env, "SPEED", 0.15);
        density = getDouble(env, "DENSITY", 0.25);

        boolean[][] grid = createGrid();

        while (true) {
            render(grid);
            grid = nextGeneration(grid);
            Thread.sleep((long) (speed * 1000));
        }
    }

    static Map<String, String> loadEnv(String filename) {
        Map<String, String> values = new HashMap<>();

        try {
            for (String line : Files.readAllLines(Path.of(filename))) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    values.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException ignored) {
            // Use defaults if ../.env does not exist
        }

        return values;
    }

    static int getInt(Map<String, String> env, String key, int fallback) {
        try {
            return Integer.parseInt(env.getOrDefault(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static double getDouble(Map<String, String> env, String key, double fallback) {
        try {
            return Double.parseDouble(env.getOrDefault(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static boolean[][] createGrid() {
        boolean[][] grid = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = random.nextDouble() < density;
            }
        }

        return grid;
    }

    static int countNeighbors(boolean[][] grid, int x, int y) {
        int count = 0;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && grid[ny][nx]) {
                    count++;
                }
            }
        }

        return count;
    }

    static boolean[][] nextGeneration(boolean[][] grid) {
        boolean[][] next = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean alive = grid[y][x];
                int neighbors = countNeighbors(grid, x, y);

                next[y][x] =
                        (alive && (neighbors == 2 || neighbors == 3)) ||
                                (!alive && neighbors == 3);
            }
        }

        return next;
    }

    static void render(boolean[][] grid) {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        for (boolean[] row : grid) {
            for (boolean cell : row) {
                System.out.print(cell ? "█" : " ");
            }
            System.out.println();
        }
    }
}