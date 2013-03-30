package com.girarda.sudosolve.sudoku;

public class Cell {

	private int number;
	private boolean fixed = false;

	public Cell() {

	}

	public Cell(int number) {
		this.number = number;
		if (number != 0)
			this.fixed = true;
	}

	public boolean isEmpty() {
		return number == 0;
	}

	public void assignNumber(int newNumber) {
		number = newNumber;
	}

	public int getNumber() {
		return number;
	}

	public void empty() {
		number = 0;
	}

	public boolean isFixed() {
		return fixed;
	}

}
