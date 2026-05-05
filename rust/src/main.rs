use dotenvy::from_path;
use std::{env, thread, time::Duration};
use rand::RngExt;

type Grid = Vec<Vec<bool>>;

fn env_usize(key: &str, default: usize) -> usize {
    env::var(key)
        .ok()
        .and_then(|v| v.parse::<usize>().ok())
        .unwrap_or(default)
}

fn env_f64(key: &str, default: f64) -> f64 {
    env::var(key)
        .ok()
        .and_then(|v| v.parse::<f64>().ok())
        .unwrap_or(default)
}

fn create_grid(width: usize, height: usize, density: f64) -> Grid {
    let mut rng = rand::rng();

    (0..height)
        .map(|_| {
            (0..width)
                .map(|_| rng.random::<f64>() < density)
                .collect()
        })
        .collect()
}

fn is_alive(grid: &Grid, x: isize, y: isize) -> bool {
    if y < 0 || x < 0 {
        return false;
    }

    grid.get(y as usize)
        .and_then(|row| row.get(x as usize))
        .copied()
        .unwrap_or(false)
}

fn count_neighbors(grid: &Grid, x: usize, y: usize) -> usize {
    let mut count = 0;

    for dy in -1..=1 {
        for dx in -1..=1 {
            if dx == 0 && dy == 0 {
                continue;
            }

            if is_alive(grid, x as isize + dx, y as isize + dy) {
                count += 1;
            }
        }
    }

    count
}

fn next_cell(grid: &Grid, x: usize, y: usize) -> bool {
    let alive = grid[y][x];
    let neighbors = count_neighbors(grid, x, y);

    (alive && (neighbors == 2 || neighbors == 3)) || (!alive && neighbors == 3)
}

fn next_generation(grid: &Grid) -> Grid {
    let height = grid.len();
    let width = grid[0].len();

    (0..height)
        .map(|y| (0..width).map(|x| next_cell(grid, x, y)).collect())
        .collect()
}

fn clear_screen() {
    print!("\x1b[H\x1b[2J");
}

fn render(grid: &Grid) {
    clear_screen();

    for row in grid {
        for &cell in row {
            print!("{}", if cell { "█" } else { " " });
        }
        println!();
    }
}

fn main() {
    let _ = from_path("../.env");

    let width = env_usize("WIDTH", 40);
    let height = env_usize("HEIGHT", 20);
    let speed = env_f64("SPEED", 0.15);
    let density = env_f64("DENSITY", 0.25);

    let mut grid = create_grid(width, height, density);

    loop {
        render(&grid);
        grid = next_generation(&grid);
        thread::sleep(Duration::from_secs_f64(speed));
    }
}