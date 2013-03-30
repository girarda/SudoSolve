package com.girarda.sudosolve.sudoku;

public class Board {

	public static final int GRID_SIZE = 9;
	public static final int N_CELLS = GRID_SIZE * GRID_SIZE;

	Cell[][] grid = new Cell[GRID_SIZE][GRID_SIZE];

	public Board() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = new Cell();

			}
		}
	}

	public Board(int[][] grid) {
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				this.grid[row][col] = new Cell(grid[row][col]);
			}
		}
	}

	public boolean isCellEmpty(int row, int col) {
		return grid[row][col].isEmpty();
	}

	public void assignNumber(int row, int col, int number) {

		verifyCellNumberIsValid(row, col);
		verifyNumberIsValid(number);

		if (grid[row][col].isFixed()) {
			throw new RuntimeException("The cell [" + row + "," + col
					+ "] is fixed.");
		}
		grid[row][col].assignNumber(number);
	}

	public int getCellNumber(int row, int col) {
		verifyCellNumberIsValid(row, col);

		return grid[row][col].getNumber();
	}

	private void verifyCellNumberIsValid(int row, int col) {
		if (row >= GRID_SIZE || row < 0) {
			throw new RuntimeException("The row number " + row + " is invalid.");
		}
		if (col >= GRID_SIZE || col < 0) {
			throw new RuntimeException("The column number " + col
					+ " is invalid.");
		}
	}

	private void verifyNumberIsValid(int number) {
		if (number > GRID_SIZE || number < 0) {
			throw new RuntimeException("The number " + number + " is invalid.");
		}
	}

	public boolean isCellValid(int row, int col, int number) {
		return !isNumberPresentInRow(row, number)
				&& !isNumberPresentInCol(col, number)
				&& !isNumberPresentInBlock(row, col, number);
	}

	private boolean isNumberPresentInRow(int row, int number) {
		for (int col = 0; col < GRID_SIZE; col++) {
			if (grid[row][col].getNumber() == number) {
				return true;
			}
		}
		return false;
	}

	private boolean isNumberPresentInCol(int col, int number) {
		for (int row = 0; row < GRID_SIZE; row++) {
			if (grid[row][col].getNumber() == number) {
				return true;
			}
		}
		return false;
	}

	private boolean isNumberPresentInBlock(int row, int col, int number) {
		int block = getBlock(row, col);
		if (block == 0) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 1) {
			for (int i = 0; i < 3; i++) {
				for (int j = 3; j < 6; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 2) {
			for (int i = 0; i < 3; i++) {
				for (int j = 6; j < 9; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 3) {
			for (int i = 3; i < 6; i++) {
				for (int j = 0; j < 3; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 4) {
			for (int i = 3; i < 6; i++) {
				for (int j = 3; j < 6; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 5) {
			for (int i = 3; i < 6; i++) {
				for (int j = 6; j < 9; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 6) {
			for (int i = 6; i < 9; i++) {
				for (int j = 0; j < 3; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 7) {
			for (int i = 6; i < 9; i++) {
				for (int j = 3; j < 6; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		else if (block == 8) {
			for (int i = 6; i < 9; i++) {
				for (int j = 6; j < 9; j++) {
					if (grid[i][j].getNumber() == number) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private int getBlock(int row, int col) {
		// row col block
		// 0, 1, 2 0, 1, 2 1
		// 0, 1, 2 3, 4, 5 2
		// 0, 1, 2 6, 7, 8 3
		// 3, 4, 5 0, 1, 2 4
		// 3, 4, 5 3, 4, 5 5
		// 3, 4, 5 6, 7, 8 6
		// 6, 7, 8 0, 1, 2 7
		// 6, 7, 8 3, 4, 5 8
		// 6, 7, 8 6, 7, 8 9

		int block;
		if (row < 3)
			if (col < 3)
				block = 0;
			else if (col < 6)
				block = 1;
			else
				block = 2;
		else if (row < 6)
			if (col < 3)
				block = 3;
			else if (col < 6)
				block = 4;
			else
				block = 5;
		else if (col < 3)
			block = 6;
		else if (col < 6)
			block = 7;
		else
			block = 8;

		return block;
	}

	public boolean isCellFixed(int row, int col) {
		return grid[row][col].isFixed();
	}

	public void emptyCell(int row, int col) {
		grid[row][col].empty();
	}
}
