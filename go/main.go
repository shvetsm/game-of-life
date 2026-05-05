package main

import (
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

type Grid [][]bool

func getInt(key string, fallback int) int {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}

	return parsed
}

func getFloat(key string, fallback float64) float64 {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.ParseFloat(value, 64)
	if err != nil {
		return fallback
	}

	return parsed
}

func createGrid(width, height int, density float64) Grid {
	grid := make(Grid, height)

	for y := 0; y < height; y++ {
		grid[y] = make([]bool, width)

		for x := 0; x < width; x++ {
			grid[y][x] = rand.Float64() < density
		}
	}

	return grid
}

func countNeighbors(grid Grid, x, y int) int {
	count := 0
	height := len(grid)
	width := len(grid[0])

	for dy := -1; dy <= 1; dy++ {
		for dx := -1; dx <= 1; dx++ {
			if dx == 0 && dy == 0 {
				continue
			}

			nx := x + dx
			ny := y + dy

			if nx >= 0 && nx < width && ny >= 0 && ny < height {
				if grid[ny][nx] {
					count++
				}
			}
		}
	}

	return count
}

func nextGeneration(grid Grid) Grid {
	height := len(grid)
	width := len(grid[0])

	next := make(Grid, height)

	for y := 0; y < height; y++ {
		next[y] = make([]bool, width)

		for x := 0; x < width; x++ {
			alive := grid[y][x]
			neighbors := countNeighbors(grid, x, y)

			next[y][x] =
				(alive && (neighbors == 2 || neighbors == 3)) ||
					(!alive && neighbors == 3)
		}
	}

	return next
}

func clearScreen() {
	fmt.Print("\033[H\033[2J")
}

func render(grid Grid) {
	clearScreen()

	for _, row := range grid {
		for _, cell := range row {
			if cell {
				fmt.Print("█")
			} else {
				fmt.Print(" ")
			}
		}
		fmt.Println()
	}
}

func main() {
	_ = godotenv.Load("../.env")

	rand.Seed(time.Now().UnixNano())

	width := getInt("WIDTH", 40)
	height := getInt("HEIGHT", 20)
	speed := getFloat("SPEED", 0.15)
	density := getFloat("DENSITY", 0.25)

	grid := createGrid(width, height, density)

	for {
		render(grid)
		grid = nextGeneration(grid)
		time.Sleep(time.Duration(speed * float64(time.Second)))
	}
}