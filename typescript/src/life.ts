import * as dotenv from "dotenv";

dotenv.config({ path: "../.env" });

type Grid = boolean[][];

const width = intEnv("WIDTH", 40);
const height = intEnv("HEIGHT", 20);
const speed = floatEnv("SPEED", 0.15);
const density = floatEnv("DENSITY", 0.25);

function intEnv(name: string, fallback: number): number {
    const value = process.env[name];
    const parsed = value ? Number.parseInt(value, 10) : NaN;
    return Number.isNaN(parsed) ? fallback : parsed;
}

function floatEnv(name: string, fallback: number): number {
    const value = process.env[name];
    const parsed = value ? Number.parseFloat(value) : NaN;
    return Number.isNaN(parsed) ? fallback : parsed;
}

function createGrid(): Grid {
    return Array.from({ length: height }, () =>
        Array.from({ length: width }, () => Math.random() < density)
    );
}

function isAlive(grid: Grid, x: number, y: number): boolean {
    return grid[y]?.[x] ?? false;
}

function countNeighbors(grid: Grid, x: number, y: number): number {
    let count = 0;

    for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
            if (dx === 0 && dy === 0) continue;

            if (isAlive(grid, x + dx, y + dy)) {
                count++;
            }
        }
    }

    return count;
}

function nextCell(grid: Grid, x: number, y: number): boolean {
    const alive = isAlive(grid, x, y);
    const neighbors = countNeighbors(grid, x, y);

    return (
        (alive && (neighbors === 2 || neighbors === 3)) ||
        (!alive && neighbors === 3)
    );
}

function nextGeneration(grid: Grid): Grid {
    return Array.from({ length: height }, (_, y) =>
        Array.from({ length: width }, (_, x) => nextCell(grid, x, y))
    );
}

function clearScreen(): void {
    process.stdout.write("\x1b[H\x1b[2J");
}

function render(grid: Grid): void {
    clearScreen();

    for (const row of grid) {
        console.log(row.map(cell => (cell ? "█" : " ")).join(""));
    }
}

let grid = createGrid();

setInterval(() => {
    render(grid);
    grid = nextGeneration(grid);
}, speed * 1000);