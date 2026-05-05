const std = @import("std");

pub const EnvMap = std.StringHashMap([]const u8);

pub fn load(allocator: std.mem.Allocator, path: []const u8) !EnvMap {
    var env = EnvMap.init(allocator);

    const file = std.fs.cwd().openFile(path, .{}) catch {
        return env; // no file → empty map
    };
    defer file.close();

    const contents = try file.readToEndAlloc(allocator, 1024 * 1024);
    defer allocator.free(contents);

    var lines = std.mem.splitScalar(u8, contents, '\n');

    while (lines.next()) |raw_line| {
        const line = std.mem.trim(u8, raw_line, " \t\r");

        if (line.len == 0 or line[0] == '#') continue;

        if (std.mem.indexOfScalar(u8, line, '=')) |eq| {
            const key = std.mem.trim(u8, line[0..eq], " \t");
            const value = std.mem.trim(u8, line[eq + 1 ..], " \t");

            try env.put(
                try allocator.dupe(u8, key),
                try allocator.dupe(u8, value),
            );
        }
    }

    return env;
}

pub fn free(allocator: std.mem.Allocator, env: *EnvMap) void {
    var it = env.iterator();

    while (it.next()) |entry| {
        allocator.free(entry.key_ptr.*);
        allocator.free(entry.value_ptr.*);
    }

    env.deinit();
}

pub fn getInt(env: EnvMap, key: []const u8, fallback: usize) usize {
    if (env.get(key)) |v| {
        return std.fmt.parseInt(usize, v, 10) catch fallback;
    }
    return fallback;
}

pub fn getFloat(env: EnvMap, key: []const u8, fallback: f64) f64 {
    if (env.get(key)) |v| {
        return std.fmt.parseFloat(f64, v) catch fallback;
    }
    return fallback;
}