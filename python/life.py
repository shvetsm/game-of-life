import random
import os
import time
from dotenv import load_dotenv

# Load .env from parent directory
load_dotenv(dotenv_path="../.env")

WIDTH = int(os.getenv("WIDTH", 40))
HEIGHT = int(os.getenv("HEIGHT", 20))
SPEED = float(os.getenv("SPEED", 0.15))
DENSITY = float(os.getenv("DENSITY", 0.75))


def create_grid():
    return [
        [1 if random.random() < DENSITY else 0 for _ in range(WIDTH)]
        for _ in range(HEIGHT)
    ]


def count_neighbors(grid, x, y):
    count = 0

    for dy in [-1, 0, 1]:
        for dx in [-1, 0, 1]:
            if dx == 0 and dy == 0:
                continue

            nx = x + dx
            ny = y + dy

            if 0 <= nx < WIDTH and 0 <= ny < HEIGHT:
                count += grid[ny][nx]

    return count


def next_generation(grid):
    return [
        [
            1
            if (grid[y][x] == 1 and count_neighbors(grid, x, y) in [2, 3])
               or (grid[y][x] == 0 and count_neighbors(grid, x, y) == 3)
            else 0
            for x in range(WIDTH)
        ]
        for y in range(HEIGHT)
    ]


def clear():
    os.system("cls" if os.name == "nt" else "clear")


def render(grid):
    clear()
    for row in grid:
        print("".join("█" if cell else " " for cell in row))


grid = create_grid()

while True:
    render(grid)
    grid = next_generation(grid)
    time.sleep(SPEED)