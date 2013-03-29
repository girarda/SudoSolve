package com.girarda.sudosolve.sudograb;

import java.io.File;
import java.io.FileOutputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvKNearest;
import org.opencv.ml.CvSVM;

import android.os.Environment;


public class DigitRecognizer {
	CvSVM svm = new CvSVM();
	int classes = 10;
	int trainSamples = 50;
	int size = 40;
	CvKNearest knn = new CvKNearest();
	Mat trainData = new Mat(trainSamples*classes, size*size, CvType.CV_32FC1);
	Mat trainClasses = new Mat(trainSamples*classes, 1, CvType.CV_32FC1);
	int K = 10;
	
	File dirPaths = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	



	public DigitRecognizer() {
		getData();
		//train();
		//test();
	}

	private void getData() {
		for (int number = 0; number <= classes; number++) {
			for (int sample = 0; sample <= trainSamples; sample++) {
			}
		}

	}

	public void train() {
		knn = new CvKNearest(trainData, trainClasses, new Mat(), false, K);
	}


	public void test() {
	}
}