package com.girarda.sudosolve.sudokusolver;

import com.girarda.sudosolve.sudoku.Board;

public class SudokuSolver {

	public static boolean solveBackTracking(Board sudokuBoard) {
		return solveBackTracking(sudokuBoard, 0, 0);
	}

	public static boolean solveBackTracking(Board sudokuBoard, int row, int col) {
		if (row == Board.GRID_SIZE) {
			row = 0;
			if (++col == Board.GRID_SIZE)
				return true;
		}
		if (!sudokuBoard.isCellEmpty(row, col)) // skip filled cells
			return solveBackTracking(sudokuBoard, row + 1, col);

		for (int val = 1; val <= Board.GRID_SIZE; ++val) {
			if (sudokuBoard.isCellValid(row, col, val)) {
				sudokuBoard.assignNumber(row, col, val);
				if (solveBackTracking(sudokuBoard, row + 1, col))
					return true;
			}
		}
		sudokuBoard.emptyCell(row, col); // reset on backtrack
		return false;
	}
}
