package com.girarda.sudosolve.sudograb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvKNearest;
import org.opencv.ml.CvSVM;


public class DigitRecognizer {
	CvSVM svm = new CvSVM();
	int classes = 10;
	int trainSamples = 50;
	int size = 40;
	String fileDirectory = "../OCR/";
	CvKNearest knn = new CvKNearest();
	Mat trainData = new Mat(trainSamples*classes, size*size, CvType.CV_32FC1);
	Mat trainClasses = new Mat(trainSamples*classes, 1, CvType.CV_32FC1);
	int K = 10;


	public DigitRecognizer() {
		//getData();
		//train();
		//test();
	}

	private void getData() {

	}

	public void train() {
		knn = new CvKNearest(trainData, trainClasses, new Mat(), false, K);
	}

	public float classify(Mat img, int showResult) {

	}

	public void test() {

}