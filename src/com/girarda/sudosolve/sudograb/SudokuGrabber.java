package com.girarda.sudosolve.sudograb;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
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

public class SudokuGrabber {

	private Bitmap originalImg;
	private Mat imgMatrix = new Mat();
	private Mat intermediateMat;
	
	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		intermediateMat = new Mat(originalImg.getHeight(), originalImg.getWidth(), CvType.CV_32F);
		bitmapToMatrix(originalImg, imgMatrix);
	}

	private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
		Utils.bitmapToMat(bitmapImg, matrix);
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
	}

	public Bitmap getSolvedSudoku() {
		applyThreshold(imgMatrix);
		Point[] sudokuGrid =detectSudokuGrid(intermediateMat);
		Point[] corners = detectCorners(intermediateMat, sudokuGrid);
		return getConvertedResult();
	}

	private void applyThreshold(Mat matrix) {
		// TODO Change size to (9,9)? it is slower but it finds the contour of the second picture.
		Imgproc.GaussianBlur(matrix, matrix, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(matrix, intermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		Core.bitwise_not(intermediateMat, intermediateMat);

		dilate(intermediateMat);

		Imgproc.cvtColor(intermediateMat, matrix, Imgproc.COLOR_GRAY2BGRA, 4);

	}

	private void dilate(Mat matrix) {
		// TODO add erode method to cancel dilate?
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(matrix, matrix, kernel);
	}


	private Point[] detectSudokuGrid(Mat matrix) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(matrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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
		for (int i = 0; i < contours.size(); i++) {
			if (i == maxAreaIdx) {
				Imgproc.drawContours(matrix, contours, maxAreaIdx, new Scalar(255, 255 ,255));
			}
			else {
				Imgproc.drawContours(matrix, contours, i, new Scalar(0, 0 ,0));
			}
		}

		return contours.get(maxAreaIdx).toArray();
	}

	private Point[] detectCorners(Mat matrix, Point[] points) {
		MatOfPoint2f approxCurve =  new MatOfPoint2f();
		MatOfPoint2f source = new MatOfPoint2f(points);
		Imgproc.approxPolyDP(source, approxCurve, 100.0, true);
		
		Point[] corners = approxCurve.toArray();
		for (Point p: corners) {
			Core.circle(matrix, p, 100, new Scalar(255,0,0));
		}
		return corners;
	}		

	private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
		Mat result = new Mat();
		Imgproc.cvtColor(matrix, result, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(result, bitmap);
	}

	public Bitmap getConvertedResult() {
		matrixToBitmap(intermediateMat, originalImg);
		return originalImg;
	}

}
