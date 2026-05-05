import java.io.File
import kotlin.random.Random

typealias Grid = List<List<Boolean>>

fun loadEnv(path: String): Map<String, String> {
    val file = File(path)
    if (!file.exists()) return emptyMap()

    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }
        .toMap()
}

val env = loadEnv("../.env")

val width = env["WIDTH"]?.toIntOrNull() ?: 40
val height = env["HEIGHT"]?.toIntOrNull() ?: 20
val speed = env["SPEED"]?.toDoubleOrNull() ?: 0.15
val density = env["DENSITY"]?.toDoubleOrNull() ?: 0.25

fun createGrid(): Grid =
    List(height) {
        List(width) {
            Random.nextDouble() < density
        }
    }

fun isAlive(grid: Grid, x: Int, y: Int): Boolean =
    grid.getOrNull(y)?.getOrNull(x) ?: false

fun countNeighbors(grid: Grid, x: Int, y: Int): Int {
    var count = 0

    for (dy in -1..1) {
        for (dx in -1..1) {
            if (dx == 0 && dy == 0) continue

            if (isAlive(grid, x + dx, y + dy)) {
                count++
            }
        }
    }

    return count
}

fun nextCell(grid: Grid, x: Int, y: Int): Boolean {
    val alive = isAlive(grid, x, y)
    val neighbors = countNeighbors(grid, x, y)

    return (alive && neighbors in 2..3) ||
            (!alive && neighbors == 3)
}

fun nextGeneration(grid: Grid): Grid =
    List(height) { y ->
        List(width) { x ->
            nextCell(grid, x, y)
        }
    }

fun clearScreen() {
    print("\u001b[H\u001b[2J")
    System.out.flush()
}

fun render(grid: Grid) {
    clearScreen()

    for (row in grid) {
        println(row.joinToString("") { cell ->
            if (cell) "█" else " "
        })
    }
}

fun main() {
    var grid = createGrid()

    while (true) {
        render(grid)
        grid = nextGeneration(grid)
        Thread.sleep((speed * 1000).toLong())
    }
}