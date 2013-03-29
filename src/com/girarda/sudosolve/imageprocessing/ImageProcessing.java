package com.girarda.sudosolve.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.googlecode.leptonica.android.Enhance;

import android.graphics.Bitmap;

public class ImageProcessing {
	

	public static void convertBGR2Gray(Mat matrix) {
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
	}

	public static void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
		Utils.bitmapToMat(bitmapImg, matrix);
	}
	
	public static void matrixToBitmap(Mat matrix, Bitmap bitmap) {
		Utils.matToBitmap(matrix, bitmap);
	}
	
	public static void dilate(Mat matrix) {
		// TODO add erode method to cancel dilate?
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(matrix, matrix, kernel);
	}
	
	public static void applyThreshold(Mat matrix) {
		// TODO Change size to (9,9)? it is slower but it finds the contour of the second picture.

		matrix.assignTo(matrix, CvType.CV_8UC1);

		Imgproc.GaussianBlur(matrix, matrix, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(matrix, matrix, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		Core.bitwise_not(matrix, matrix);

		dilate(matrix);
	}
	
	public static Mat GetIsolatedBiggestContour(Mat img) {
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

	public static MatOfPoint findBiggestContour(List<MatOfPoint> contours) {
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
	
	public static Mat preprosessForOCR(Mat imgSrc, int newWidth, int newHeight) {
		Mat result = new Mat(imgSrc.size(), CvType.CV_32F);
		imgSrc.assignTo(result, CvType.CV_32F);
		Mat scaledResult = new Mat();

		convertBGR2Gray(result);

		result.assignTo(result, CvType.CV_8UC1);

		Imgproc.GaussianBlur(result, result, new Size(11, 11), 0);

		Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		
		Imgproc.Canny(result, new Mat(), Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY);
		
		result = result.submat(10, result.width()-10, 10, result.height()-10);
		
		//dilate(result);
		
		//result = GetIsolatedBiggestContour(result);
		Imgproc.resize(result, scaledResult, new Size(newWidth, newHeight));
		return scaledResult;
	}

}
