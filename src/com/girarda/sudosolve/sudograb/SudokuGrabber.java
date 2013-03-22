package com.girarda.sudosolve.sudograb;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;

public class SudokuGrabber {
	
	private Bitmap originalImg;
	private Mat matrix = new Mat();
	private Mat intermediateMat;
	
	public SudokuGrabber(Bitmap bitmapImg) {
		originalImg = bitmapImg;
		intermediateMat = new Mat(originalImg.getHeight(), originalImg.getWidth(), CvType.CV_8UC1);
		bitmapToMatrix(originalImg, matrix);
	}
	
    private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
    	Utils.bitmapToMat(bitmapImg, matrix);
    	Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2GRAY);
    }
    
    public Bitmap getSolvedSudoku() {
    	applyCannyFilter(matrix);
    	detectSudokuGrid(intermediateMat);
    	return getConvertedResult();
    }

	private void applyCannyFilter(Mat matrix) {
		Imgproc.GaussianBlur(matrix, matrix, new Size(11, 11), 0);
		
		Imgproc.adaptiveThreshold(matrix, intermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
    	Core.bitwise_not(intermediateMat, intermediateMat);
    	
        Imgproc.cvtColor(intermediateMat, matrix, Imgproc.COLOR_GRAY2BGRA, 4);

	}
	
    private void detectSudokuGrid(Mat matrix) {
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Imgproc.findContours(matrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    	
    	double maxArea = -1;
    	int maxAreaIdx = -1;
    	
    	for (int i = 0; i < contours.size(); i++) {
    		Mat contour = contours.get(i);
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
	}
    
    private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
    	Mat result = new Mat();
    	Imgproc.cvtColor(intermediateMat, result, Imgproc.COLOR_GRAY2BGRA);
    	Utils.matToBitmap(result, bitmap);
    }
    
    public Bitmap getConvertedResult() {
    	matrixToBitmap(matrix, originalImg);
    	return originalImg;
    }

}
