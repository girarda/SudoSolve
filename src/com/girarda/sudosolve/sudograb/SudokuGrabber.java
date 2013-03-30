package com.girarda.sudosolve.sudograb;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;

import com.girarda.sudosolve.imageprocessing.ImageProcessing;
import com.girarda.sudosolve.sudoku.Board;
import com.girarda.sudosolve.sudokusolver.SudokuSolver;

public class SudokuGrabber {

	private Bitmap originalImg;
	private Mat imgMatrix = new Mat();
	private Mat intermediateMat;
	private Mat newImg;
	private DigitRecognizer digitRec = new DigitRecognizer();

	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		ImageProcessing.bitmapToMatrix(originalImg, imgMatrix);
		intermediateMat = imgMatrix.clone();
		ImageProcessing.convertBGR2Gray(intermediateMat);
	}

	public Bitmap getSolvedSudoku() {
		ImageProcessing.applyThreshold(intermediateMat);
		Point[] sudokuGrid = detectSudokuGrid(intermediateMat);
		Point[] corners = detectCorners(intermediateMat, sudokuGrid);
		newImg = warpSudokuGrid(corners, imgMatrix);
		Mat[][] cells = getCells(newImg);
		int[][] cellNumbers = digitRec.getSudokuNumbers(cells);

		intermediateMat = null;
		cells = null;
		Board board = new Board(cellNumbers);
		printSolution(board);
		return getConvertedResult();
	}

	private void printSolution(Board board) {
		SudokuSolver.solveBackTracking(board);
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				if (!board.isCellFixed(row, col)) {
					Integer n = Integer.valueOf(board.getCellNumber(row, col));
					String text = n.toString();
					System.out.println(text);
					Core.putText(newImg, text,
							new Point(12 + (newImg.width()+1)/9*col,35 + (newImg.height()+1)/9*row),
							Core.FONT_HERSHEY_SCRIPT_SIMPLEX,1 , new Scalar(255,255,255));
				}
			}
		}
	}

	private Point[] detectSudokuGrid(Mat matrix) {
		ImageProcessing.GetIsolatedBiggestContour(matrix);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(matrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		return ImageProcessing.findBiggestContour(contours).toArray();
	}

	private Point[] detectCorners(Mat matrix, Point[] points) {
		// Returned corners order in array:
		// 1 0
		// 2 3

		MatOfPoint2f approxCurve =  new MatOfPoint2f();
		MatOfPoint2f source = new MatOfPoint2f(points);
		Imgproc.approxPolyDP(source, approxCurve, 25.0, true);

		Point[] corners = approxCurve.toArray();
		for (int i = 0; i < corners.length; i++) {
			Core.circle(matrix, corners[i], 10, new Scalar(255,0,0));
		}
		return corners;
	}

	private Mat warpSudokuGrid(Point[] corners, Mat matrix) {
		Point[] src = new Point[4];
		Point[] dst = new Point[4];
		
		int maxLength = rectify(corners, src, dst);
		
		Mat srcMat = new Mat(4,1, CvType.CV_32FC2);
		Mat dstMat = new Mat(4,1, CvType.CV_32FC2);
		for (int i = 0; i < src.length; i++) {
			srcMat.put(i, 0, new double[]{src[i].x, src[i].y});
			dstMat.put(i, 0, new double[]{dst[i].x, dst[i].y});
		}

		Mat undistortedGrid = new Mat((int)maxLength, (int)maxLength, CvType.CV_32SC1);
		Imgproc.warpPerspective(matrix, undistortedGrid, Imgproc.getPerspectiveTransform(srcMat, dstMat), new Size(maxLength, maxLength));

		return undistortedGrid;
	}
	
	private int rectify(Point[] corners, Point[] src, Point[] dst) {
		Point ptTopLeft = corners[1];			Point ptTopRight = corners[0];
		Point ptBottomLeft= corners[2];			Point ptBottomRight = corners[3];

		int maxLength = (int) getMaxLength(ptTopLeft, ptTopRight, ptBottomLeft, ptBottomRight);
		
		src[0] = ptTopLeft;            dst[0] = new Point(0,0);
		src[1] = ptTopRight;        dst[1] = new Point(maxLength-1, 0);
		src[2] = ptBottomRight;        dst[2] = new Point(maxLength-1, maxLength-1);
		src[3] = ptBottomLeft;        dst[3] = new Point(0, maxLength-1);
		
		return maxLength;
	}
	
	private double getMaxLength(Point ptTopLeft, Point ptTopRight, Point ptBottomLeft, Point ptBottomRight) {
		double maxLength = (ptBottomLeft.x-ptBottomRight.x)*(ptBottomLeft.x-ptBottomRight.x) + (ptBottomLeft.y-ptBottomRight.y)*(ptBottomLeft.y-ptBottomRight.y);
		double temp = (ptTopRight.x-ptBottomRight.x)*(ptTopRight.x-ptBottomRight.x) + (ptTopRight.y-ptBottomRight.y)*(ptTopRight.y-ptBottomRight.y);
		if(temp > maxLength) {
			maxLength = temp;
		}

		temp = (ptTopRight.x-ptTopLeft.x)*(ptTopRight.x-ptTopLeft.x) + (ptTopRight.y-ptTopLeft.y)*(ptTopRight.y-ptTopLeft.y);
		if(temp > maxLength) {
			maxLength = temp;
		}

		temp = (ptBottomLeft.x-ptTopLeft.x)*(ptBottomLeft.x-ptTopLeft.x) + (ptBottomLeft.y-ptTopLeft.y)*(ptBottomLeft.y-ptTopLeft.y);
		if(temp > maxLength) {
			maxLength = temp;
		}

		maxLength = Math.sqrt((double)maxLength);
		return maxLength;
	}

	private Mat[][] getCells(Mat undistortedGrid) {
		Mat[][] cells = new Mat[10][10];
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				cells[row][col] = undistortedGrid.submat(undistortedGrid.rows()*row/9,undistortedGrid.rows()*(row+1)/9,undistortedGrid.cols()*col/9,undistortedGrid.cols()*(col+1)/9);
				cells[row][col] = ImageProcessing.preprosessForOCR(cells[row][col], 100,100);
			}
		}
		return cells;
	}

	public Bitmap getConvertedResult() {
		Bitmap newBitmap = Bitmap.createBitmap(newImg.width(), newImg.height(), originalImg.getConfig());
		ImageProcessing.matrixToBitmap(newImg, newBitmap);
		return newBitmap;
	}
}
