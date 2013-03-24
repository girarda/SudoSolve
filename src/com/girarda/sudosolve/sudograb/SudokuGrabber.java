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
	private Mat newImg;
	
	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		intermediateMat = new Mat(originalImg.getHeight(), originalImg.getWidth(), CvType.CV_32FC2);
		bitmapToMatrix(originalImg, imgMatrix);
		intermediateMat = imgMatrix.clone();
	}

	private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
		Utils.bitmapToMat(bitmapImg, matrix);
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
	}

	public Bitmap getSolvedSudoku() {
		applyThreshold(imgMatrix);
		Point[] sudokuGrid =detectSudokuGrid(intermediateMat);
		Point[] corners = detectCorners(intermediateMat, sudokuGrid);
		newImg = warpSudokuGrid(corners, imgMatrix);
		return getConvertedResult();
	}

	private void applyThreshold(Mat matrix) {
		// TODO Change size to (9,9)? it is slower but it finds the contour of the second picture.
		Imgproc.GaussianBlur(intermediateMat, intermediateMat, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(intermediateMat, intermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		Core.bitwise_not(intermediateMat, intermediateMat);

		dilate(intermediateMat);

		//Imgproc.cvtColor(intermediateMat, matrix, Imgproc.COLOR_GRAY2BGRA, 4);

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
	    
		Mat undistorted = new Mat((int)maxLength, (int)maxLength, CvType.CV_32FC2);
	    Imgproc.warpPerspective(matrix, undistorted, Imgproc.getPerspectiveTransform(srcMat, dstMat), new Size(maxLength, maxLength));
	    return undistorted;
	}

	private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
		Mat result = new Mat();
		//Imgproc.cvtColor(matrix, result, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(matrix, bitmap);
	}

	public Bitmap getConvertedResult() {
		Bitmap newBitmap = Bitmap.createBitmap(newImg.width(), newImg.height(), originalImg.getConfig());
		matrixToBitmap(newImg, newBitmap);
		return newBitmap;
	}

}
