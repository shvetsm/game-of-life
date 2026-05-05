const std = @import("std");
const envmod = @import("env.zig");

const Grid = []bool;

fn isAlive(grid: Grid, width: usize, height: usize, x: isize, y: isize) bool {
    if (x < 0 or y < 0) return false;

    const ux: usize = @intCast(x);
    const uy: usize = @intCast(y);

    if (ux >= width or uy >= height) return false;

    return grid[uy * width + ux];
}

fn countNeighbors(grid: Grid, width: usize, height: usize, x: usize, y: usize) usize {
    var count: usize = 0;

    var dy: isize = -1;
    while (dy <= 1) : (dy += 1) {
        var dx: isize = -1;
        while (dx <= 1) : (dx += 1) {
            if (dx == 0 and dy == 0) continue;

            if (isAlive(grid, width, height,
                @as(isize, @intCast(x)) + dx,
                @as(isize, @intCast(y)) + dy))
                {
                    count += 1;
                }
        }
    }

    return count;
}

fn nextGeneration(current: Grid, next: Grid, width: usize, height: usize) void {
    var y: usize = 0;
    while (y < height) : (y += 1) {
        var x: usize = 0;
        while (x < width) : (x += 1) {
            const alive = current[y * width + x];
            const neighbors = countNeighbors(current, width, height, x, y);

            next[y * width + x] =
            (alive and (neighbors == 2 or neighbors == 3)) or
                (!alive and neighbors == 3);
        }
    }
}

fn render(grid: Grid, width: usize, height: usize) void {
    std.debug.print("\x1b[H\x1b[2J", .{});

    var y: usize = 0;
    while (y < height) : (y += 1) {
        var x: usize = 0;
        while (x < width) : (x += 1) {
            std.debug.print("{s}", .{if (grid[y * width + x]) "█" else " "});
        }
        std.debug.print("\n", .{});
    }
}

pub fn main() !void {
    const allocator = std.heap.page_allocator;

    var env = try envmod.load(allocator, "../.env");
    defer envmod.free(allocator, &env);

    const width = envmod.getInt(env, "WIDTH", 40);
    const height = envmod.getInt(env, "HEIGHT", 20);
    const speed = envmod.getFloat(env, "SPEED", 0.15);
    const density = envmod.getFloat(env, "DENSITY", 0.25);

    var current = try allocator.alloc(bool, width * height);
    defer allocator.free(current);

    var next = try allocator.alloc(bool, width * height);
    defer allocator.free(next);

    var prng = std.Random.DefaultPrng.init(@intCast(std.time.timestamp()));
    const random = prng.random();

    for (current) |*cell| {
        cell.* = random.float(f64) < density;
    }

    while (true) {
        render(current, width, height);
        nextGeneration(current, next, width, height);

        const tmp = current;
        current = next;
        next = tmp;

        std.time.sleep(@intFromFloat(speed * std.time.ns_per_s));
    }
}