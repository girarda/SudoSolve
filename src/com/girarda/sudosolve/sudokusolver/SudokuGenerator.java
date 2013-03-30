package com.girarda.sudosolve.sudokusolver;

public class SudokuGenerator {
	public static int[][] getUnsolvableGrid() {
		int[][] grid = new int[9][9];

		grid[0][0] = 8;

		grid[1][2] = 6;
		grid[1][3] = 6;

		grid[2][1] = 7;
		grid[2][4] = 9;
		grid[2][6] = 2;

		grid[3][1] = 5;
		grid[3][5] = 7;

		grid[4][4] = 4;
		grid[4][5] = 5;
		grid[4][6] = 7;

		grid[5][3] = 1;
		grid[5][7] = 3;

		grid[6][2] = 1;
		grid[6][7] = 6;
		grid[6][8] = 8;

		grid[7][2] = 8;
		grid[7][3] = 5;
		grid[7][7] = 1;
		grid[8][1] = 9;
		grid[8][6] = 4;

		return grid;
	}

	public static int[][] getHardGrid() {
		int[][] grid = new int[9][9];

		grid[0][0] = 8;

		grid[1][2] = 3;
		grid[1][3] = 6;

		grid[2][1] = 7;
		grid[2][4] = 9;
		grid[2][6] = 2;

		grid[3][1] = 5;
		grid[3][5] = 7;

		grid[4][4] = 4;
		grid[4][5] = 5;
		grid[4][6] = 7;

		grid[5][3] = 1;
		grid[5][7] = 3;

		grid[6][2] = 1;
		grid[6][7] = 6;
		grid[6][8] = 8;

		grid[7][2] = 8;
		grid[7][3] = 5;
		grid[7][7] = 1;
		grid[8][1] = 9;
		grid[8][6] = 4;

		return grid;
	}

	public static int[][] getHardestGrid() {
		int[][] grid = new int[9][9];

		grid[0][0] = 9;
		grid[0][1] = 1;
		grid[0][2] = 4;

		grid[1][1] = 6;
		grid[1][4] = 1;
		grid[1][5] = 9;

		grid[2][3] = 3;
		grid[2][6] = 6;

		grid[3][0] = 2;
		grid[3][5] = 6;
		grid[3][6] = 8;

		grid[4][3] = 5;
		grid[4][4] = 2;
		grid[4][5] = 1;

		grid[5][2] = 5;
		grid[5][3] = 9;
		grid[5][8] = 7;

		grid[6][2] = 9;
		grid[6][5] = 5;

		grid[7][3] = 2;
		grid[7][4] = 4;
		grid[7][7] = 6;

		grid[8][6] = 2;
		grid[8][7] = 7;
		grid[8][8] = 5;

		return grid;
	}

}
