package com.girarda.sudosolve.sudograb;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.core.Point;

import android.graphics.Bitmap;

public class SudokuGrabber {

	private Bitmap originalImg;
	private Mat imgMatrix = new Mat();
	private Mat intermediateMat;
	private Mat newImg;
	private CvSVM svm = new CvSVM();


	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		bitmapToMatrix(originalImg, imgMatrix);
		intermediateMat = imgMatrix.clone();
		convertBGR2Gray(intermediateMat);
	}

	private void convertBGR2Gray(Mat matrix) {
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
	}

	private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
		Utils.bitmapToMat(bitmapImg, matrix);
	}

	public Bitmap getSolvedSudoku() {
		applyThreshold(intermediateMat);
		Point[] sudokuGrid = detectSudokuGrid(intermediateMat);
		Point[] corners = detectCorners(intermediateMat, sudokuGrid);
		newImg = warpSudokuGrid(corners, imgMatrix);
		Mat[][] cells = getCells(newImg);
		return getConvertedResult();
	}

	private void applyThreshold(Mat matrix) {
		// TODO Change size to (9,9)? it is slower but it finds the contour of the second picture.

		matrix.assignTo(matrix, CvType.CV_8UC1);

		Imgproc.GaussianBlur(matrix, matrix, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(matrix, matrix, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		Core.bitwise_not(matrix, matrix);

		dilate(matrix);
	}

	private void dilate(Mat matrix) {
		// TODO add erode method to cancel dilate?
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(matrix, matrix, kernel);
	}


	private Point[] detectSudokuGrid(Mat matrix) {
		GetIsolatedBiggestContour(matrix);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(matrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		return findBiggestContour(contours).toArray();
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
		Point ptTopLeft = corners[1];			Point ptTopRight = corners[0];
		Point ptBottomLeft= corners[2];			Point ptBottomRight = corners[3];

		double maxLength = (ptBottomLeft.x-ptBottomRight.x)*(ptBottomLeft.x-ptBottomRight.x) + (ptBottomLeft.y-ptBottomRight.y)*(ptBottomLeft.y-ptBottomRight.y);
		double temp = (ptTopRight.x-ptBottomRight.x)*(ptTopRight.x-ptBottomRight.x) + (ptTopRight.y-ptBottomRight.y)*(ptTopRight.y-ptBottomRight.y);
		if(temp > maxLength) maxLength = temp;

		temp = (ptTopRight.x-ptTopLeft.x)*(ptTopRight.x-ptTopLeft.x) + (ptTopRight.y-ptTopLeft.y)*(ptTopRight.y-ptTopLeft.y);
		if(temp > maxLength) maxLength = temp;

		temp = (ptBottomLeft.x-ptTopLeft.x)*(ptBottomLeft.x-ptTopLeft.x) + (ptBottomLeft.y-ptTopLeft.y)*(ptBottomLeft.y-ptTopLeft.y);
		if(temp > maxLength) maxLength = temp;

		maxLength = Math.sqrt((double)maxLength);

		Point[] src = new Point[4];
		Point[] dst = new Point[4];
		src[0] = ptTopLeft;            dst[0] = new Point(0,0);
		src[1] = ptTopRight;        dst[1] = new Point(maxLength-1, 0);
		src[2] = ptBottomRight;        dst[2] = new Point(maxLength-1, maxLength-1);
		src[3] = ptBottomLeft;        dst[3] = new Point(0, maxLength-1);

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

	private Mat[][] getCells(Mat undistortedGrid) {
		Mat[][] cells = new Mat[10][10];
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				cells[row][col] = undistortedGrid.submat(undistortedGrid.rows()*row/9,undistortedGrid.rows()*(row+1)/9,undistortedGrid.cols()*col/9,undistortedGrid.cols()*(col+1)/9);
				cells[row][col] = preprosess(cells[row][col], 10, 10);
			}
		}
		return cells;
	}

	private Mat preprosess(Mat imgSrc, int newWidth, int newHeight) {
		Mat result = new Mat(imgSrc.size(), CvType.CV_32F);
		imgSrc.assignTo(result, CvType.CV_32F);
		Mat scaledResult = new Mat();

		convertBGR2Gray(result);
		applyThreshold(result);

		result = GetIsolatedBiggestContour(result);

		Imgproc.resize(result, scaledResult, new Size(newWidth, newHeight));
		return scaledResult;
	}

	private Mat GetIsolatedBiggestContour(Mat img) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(img, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		MatOfPoint biggestContour = findBiggestContour(contours);

		if (biggestContour == null) {
			// No contour in img
			return img;
		}
		for (int i = 0; i < contours.size(); i++) {
			Scalar colorToDrawContour = new Scalar(0,0,0);
			if (contours.get(i) == biggestContour) {
				colorToDrawContour = new Scalar(255,0,0);
			}
			Imgproc.drawContours(img, contours, i, colorToDrawContour);

		}
		return img.submat(Imgproc.boundingRect(biggestContour));

	}

	private MatOfPoint findBiggestContour(List<MatOfPoint> contours) {
		double maxArea = -1;
		int maxAreaIdx = -1;

		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint contour = contours.get(i);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				maxArea = contourarea;
				maxAreaIdx = i;
			}
		}
		if (maxArea != -1) {
			return contours.get(maxAreaIdx);
		}
		else {
			return null;
		}
	}

	private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
		Utils.matToBitmap(matrix, bitmap);
	}

	public Bitmap getConvertedResult() {
		Bitmap newBitmap = Bitmap.createBitmap(newImg.width(), newImg.height(), originalImg.getConfig());
		matrixToBitmap(newImg, newBitmap);
		return newBitmap;
	}
}
